package com.rota.order.application;

import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.port.PaymentGateway.PaymentOutcome;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.Collection;
import java.util.EnumSet;

/**
 * Consultas e mudanças de status. Devolve DTOs já montados dentro da transação,
 * pois itens e histórico são carregados sob demanda.
 */
@Service
@Transactional
public class OrderService {

    private static final AuthenticatedUser DELIVERY_SERVICE = AuthenticatedUser.service("delivery-service");

    private final OrderRepository orders;
    private final OrderAccessPolicy policy;
    private final Clock clock;

    public OrderService(OrderRepository orders, OrderAccessPolicy policy, Clock clock) {
        this.orders = orders;
        this.policy = policy;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public OrderResponse get(AuthenticatedUser user, Long orderId) {
        Order order = find(orderId);
        policy.checkCanView(user, order);
        return OrderResponse.from(order);
    }

    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listForCustomer(AuthenticatedUser user, Pageable pageable) {
        return orders.findByCustomerIdOrderByCreatedAtDesc(user.id(), pageable).map(OrderSummaryResponse::from);
    }

    /**
     * Pedidos de um restaurante do usuário. O filtro por dono garante que um restaurante
     * não enxergue pedidos de outro.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> listForRestaurant(AuthenticatedUser user, Long restaurantId,
                                                 Collection<OrderStatus> statuses, Pageable pageable) {
        Collection<OrderStatus> filter = statuses == null || statuses.isEmpty()
                ? EnumSet.allOf(OrderStatus.class)
                : statuses;
        return orders.findByRestaurantIdAndRestaurantOwnerIdAndStatusInOrderByCreatedAtDesc(
                restaurantId, user.id(), filter, pageable).map(OrderResponse::from);
    }

    /** Todos os pedidos da plataforma (administrador); o acesso é checado no controller. */
    @Transactional(readOnly = true)
    public Page<OrderSummaryResponse> listAll(Collection<OrderStatus> statuses, Pageable pageable) {
        Collection<OrderStatus> filter = statuses == null || statuses.isEmpty()
                ? EnumSet.allOf(OrderStatus.class)
                : statuses;
        return orders.findByStatusInOrderByCreatedAtDesc(filter, pageable).map(OrderSummaryResponse::from);
    }
    public OrderResponse changeStatus(AuthenticatedUser user, Long orderId, OrderStatus target, String reason) {
        return transition(user, orderId, target, user.id(), reason);
    }

    public void assignDriver(Long orderId, Long driverId) {
        Order order = find(orderId);
        order.assignDriver(driverId);
        orders.saveAndFlush(order);
    }

    /**
     * Avanço informado pelo delivery-service (retirada e entrega). Idempotente: se o pedido
     * já está no status, nada muda. O histórico registra o entregador como autor.
     */
    public void advanceByDelivery(Long orderId, OrderStatus target, Long driverId) {
        if (find(orderId).getStatus() == target) {
            return;
        }
        transition(DELIVERY_SERVICE, orderId, target, driverId, null);
    }

    /**
     * A abertura da corrida (pedido pronto) e as notificações saem como eventos, gravados no
     * outbox na mesma transação: nenhuma chamada a outro serviço segura esta mudança.
     */
    private OrderResponse transition(AuthenticatedUser user, Long orderId, OrderStatus target, Long actorId,
                                     String reason) {
        Order order = find(orderId);
        policy.checkCanTransition(user, order, target);
        order.transitionTo(target, actorId, reason, clock.instant());
        return OrderResponse.from(orders.saveAndFlush(order));
    }

    public OrderResponse cancel(AuthenticatedUser user, Long orderId, String reason) {
        return changeStatus(user, orderId, OrderStatus.CANCELLED, reason);
    }

    /**
     * Dados para cobrar um pedido que aguarda pagamento.
     */
    @Transactional(readOnly = true)
    public PayableOrder payable(AuthenticatedUser user, Long orderId) {
        Order order = find(orderId);
        policy.checkCanView(user, order);
        if (order.getStatus() != OrderStatus.PAYMENT_PENDING) {
            throw new ConflictException("O pedido não está aguardando pagamento");
        }
        return new PayableOrder(order.getId(), order.getCustomerId(), order.getTotal(), order.getPaymentMethod());
    }

    /**
     * Aplica o resultado da cobrança. Idempotente: se o pedido já saiu de PAYMENT_PENDING
     * (ex.: foi cancelado enquanto o pagamento processava), nada muda.
     */
    public OrderResponse applyPayment(Long orderId, PaymentOutcome outcome) {
        Order order = find(orderId);
        if (order.getStatus() == OrderStatus.PAYMENT_PENDING) {
            if (outcome.approved()) {
                order.transitionTo(OrderStatus.PAID, null, null, clock.instant());
            } else {
                order.transitionTo(OrderStatus.CANCELLED, null,
                        "Pagamento recusado: " + outcome.failureReason(), clock.instant());
            }
        }
        return OrderResponse.from(orders.saveAndFlush(order));
    }

    public record PayableOrder(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method) {
    }

    private Order find(Long orderId) {
        return orders.findById(orderId).orElseThrow(() -> NotFoundException.of("Pedido", orderId));
    }
}
