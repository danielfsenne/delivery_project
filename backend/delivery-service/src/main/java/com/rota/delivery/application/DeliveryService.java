package com.rota.delivery.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.delivery.application.port.OrderGateway;
import com.rota.delivery.domain.Delivery;
import com.rota.delivery.domain.DeliveryRepository;
import com.rota.delivery.domain.DeliveryStatus;
import com.rota.delivery.domain.Driver;
import com.rota.delivery.domain.DriverStatus;
import com.rota.delivery.domain.GeoPoint;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.CreateDeliveryRequest;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.DeliveryResponse;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.HistoryResponse;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.TrackingResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;

/**
 * Ciclo de vida das corridas. Cada passo do entregador também avança o pedido no
 * order-service dentro da mesma transação: se a chamada falhar, a corrida não muda
 * e o entregador pode tentar novamente.
 */
@Service
@Transactional
public class DeliveryService {

    private final DeliveryRepository deliveries;
    private final DriverService drivers;
    private final OrderGateway orderGateway;
    private final Clock clock;
    private final double searchRadiusKm;
    private final BigDecimal minDriverFee;

    public DeliveryService(DeliveryRepository deliveries, DriverService drivers, OrderGateway orderGateway,
                           Clock clock,
                           @Value("${rota.delivery.search-radius-km:15}") double searchRadiusKm,
                           @Value("${rota.delivery.min-driver-fee:5.00}") BigDecimal minDriverFee) {
        this.deliveries = deliveries;
        this.drivers = drivers;
        this.orderGateway = orderGateway;
        this.clock = clock;
        this.searchRadiusKm = searchRadiusKm;
        this.minDriverFee = minDriverFee;
    }

    /**
     * Idempotente por pedido: reenviar a mesma solicitação devolve a corrida existente.
     */
    public DeliveryResponse create(CreateDeliveryRequest request) {
        Delivery delivery = deliveries.findByOrderId(request.orderId()).orElseGet(() -> deliveries.save(
                new Delivery(request.orderId(), request.customerId(), request.restaurantId(),
                        request.restaurantName(), request.pickupAddress(),
                        GeoPoint.ofNullable(request.pickupLatitude(), request.pickupLongitude()),
                        request.dropoffAddress(),
                        GeoPoint.ofNullable(request.dropoffLatitude(), request.dropoffLongitude()),
                        request.deliveryFee().max(minDriverFee), clock.instant())));
        return DeliveryResponse.from(delivery, null);
    }

    /**
     * Corridas aguardando entregador, da mais próxima para a mais distante. Sem localização
     * do entregador, a lista vem por ordem de chegada. Não é somente leitura porque o
     * perfil do entregador pode ser criado no primeiro acesso.
     */
    public List<DeliveryResponse> available(Long driverId) {
        Driver driver = drivers.getOrCreate(driverId);
        if (driver.getStatus() != DriverStatus.ONLINE) {
            return List.of();
        }
        GeoPoint here = driver.location();
        return deliveries.findByStatus(DeliveryStatus.WAITING_DRIVER).stream()
                .filter(d -> here == null || d.pickup() == null || here.distanceKm(d.pickup()) <= searchRadiusKm)
                .map(d -> DeliveryResponse.from(d, here))
                .sorted(Comparator.comparing(DeliveryResponse::distanceToPickupKm,
                                Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(DeliveryResponse::createdAt))
                .toList();
    }

    public DeliveryResponse accept(Long driverId, Long deliveryId) {
        Driver driver = drivers.getOrCreate(driverId);
        if (deliveries.findFirstByDriverIdAndStatusIn(driverId, DriverService.ACTIVE).isPresent()) {
            throw new BusinessException("Você já tem uma entrega em andamento");
        }
        Delivery delivery = find(deliveryId);
        delivery.accept(driver, clock.instant());
        try {
            deliveries.saveAndFlush(delivery);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException("Esta entrega acabou de ser aceita por outro entregador");
        }
        orderGateway.assignDriver(delivery.getOrderId(), driverId);
        return DeliveryResponse.from(delivery, driver.location());
    }

    public DeliveryResponse pickUp(Long driverId, Long deliveryId) {
        Driver driver = drivers.getOrCreate(driverId);
        Delivery delivery = find(deliveryId);
        delivery.pickUp(driver, clock.instant());
        orderGateway.markOutForDelivery(delivery.getOrderId(), driverId);
        return DeliveryResponse.from(delivery, driver.location());
    }

    public DeliveryResponse complete(Long driverId, Long deliveryId) {
        Driver driver = drivers.getOrCreate(driverId);
        Delivery delivery = find(deliveryId);
        delivery.complete(driver, clock.instant());
        orderGateway.markDelivered(delivery.getOrderId(), driverId);
        return DeliveryResponse.from(delivery, driver.location());
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(Long driverId) {
        List<DeliveryResponse> done = deliveries
                .findTop50ByDriverIdAndStatusOrderByDeliveredAtDesc(driverId, DeliveryStatus.DELIVERED).stream()
                .map(d -> DeliveryResponse.from(d, null))
                .toList();
        return new HistoryResponse(deliveries.countByDriverIdAndStatus(driverId, DeliveryStatus.DELIVERED),
                deliveries.totalEarnings(driverId), done);
    }

    /** Acompanhamento do pedido pelo cliente: status da corrida e posição do entregador. */
    @Transactional(readOnly = true)
    public TrackingResponse track(AuthenticatedUser user, Long orderId) {
        Delivery delivery = deliveries.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Ainda não há entrega para o pedido " + orderId));
        boolean allowed = user.hasRole(Role.ADMIN)
                || delivery.getCustomerId().equals(user.id())
                || delivery.isAssignedTo(user.id());
        if (!allowed) {
            throw new ForbiddenException("Você não tem acesso a esta entrega");
        }
        return TrackingResponse.from(delivery);
    }

    private Delivery find(Long deliveryId) {
        return deliveries.findById(deliveryId).orElseThrow(() -> NotFoundException.of("Entrega", deliveryId));
    }
}
