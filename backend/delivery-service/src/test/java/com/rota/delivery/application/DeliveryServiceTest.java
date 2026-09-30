package com.rota.delivery.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.delivery.application.port.OrderGateway;
import com.rota.delivery.domain.Delivery;
import com.rota.delivery.domain.DeliveryRepository;
import com.rota.delivery.domain.DeliveryStatus;
import com.rota.delivery.domain.Driver;
import com.rota.delivery.domain.DriverRepository;
import com.rota.delivery.domain.GeoPoint;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.CreateDeliveryRequest;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.DeliveryResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DeliveryServiceTest {

    private static final GeoPoint DRIVER_POSITION = new GeoPoint(-20.5352, -47.4039);

    private final DeliveryRepository deliveries = mock(DeliveryRepository.class);
    private final DriverRepository driverRepository = mock(DriverRepository.class);
    private final OrderGateway orderGateway = mock(OrderGateway.class);
    private final Clock clock = Clock.systemUTC();
    private final DriverService drivers = new DriverService(driverRepository, deliveries, clock);
    private final DeliveryService service = new DeliveryService(deliveries, drivers, orderGateway, clock, 15,
            new BigDecimal("5.00"));

    private Driver driver;

    @BeforeEach
    void setUp() {
        driver = new Driver(3L, Instant.now());
        driver.goOnline(Instant.now());
        driver.moveTo(DRIVER_POSITION, Instant.now());
        when(driverRepository.findById(3L)).thenReturn(Optional.of(driver));
        when(deliveries.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(deliveries.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static Delivery delivery(long id, long orderId, GeoPoint pickup) {
        Delivery d = new Delivery(orderId, 1L, 10L, "Restaurante " + id, "Rua A", pickup, "Rua B", null,
                new BigDecimal("5.99"), Instant.now().minusSeconds(id));
        ReflectionTestUtils.setField(d, "id", id);
        return d;
    }

    @Test
    void createUsesMinimumDriverFeeAndIsIdempotent() {
        CreateDeliveryRequest request = new CreateDeliveryRequest(100L, 1L, 10L, "Forno da Nonna", "Rua A",
                null, null, "Rua B", null, null, BigDecimal.ZERO);

        DeliveryResponse created = service.create(request);
        assertThat(created.driverFee()).isEqualByComparingTo("5.00");

        Delivery existing = delivery(7L, 100L, null);
        when(deliveries.findByOrderId(100L)).thenReturn(Optional.of(existing));
        assertThat(service.create(request).id()).isEqualTo(7L);
    }

    @Test
    void availableIsSortedByDistanceAndFiltersByRadius() {
        Delivery near = delivery(1L, 101L, new GeoPoint(-20.5386, -47.4009));   // ~0,5 km
        Delivery far = delivery(2L, 102L, new GeoPoint(-20.5450, -47.4100));    // ~1,2 km
        Delivery outOfRange = delivery(3L, 103L, new GeoPoint(-21.1775, -47.8103)); // Ribeirão Preto
        when(deliveries.findByStatus(DeliveryStatus.WAITING_DRIVER)).thenReturn(List.of(far, outOfRange, near));

        List<DeliveryResponse> available = service.available(3L);

        assertThat(available).extracting(DeliveryResponse::id).containsExactly(1L, 2L);
        assertThat(available.getFirst().distanceToPickupKm()).isLessThan(1.0);
    }

    @Test
    void offlineDriverSeesNothing() {
        driver.goOffline(Instant.now());

        assertThat(service.available(3L)).isEmpty();
    }

    @Test
    void acceptAssignsDriverAndNotifiesOrder() {
        Delivery d = delivery(1L, 101L, null);
        when(deliveries.findById(1L)).thenReturn(Optional.of(d));

        service.accept(3L, 1L);

        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        verify(orderGateway).assignDriver(101L, 3L);
    }

    @Test
    void cannotAcceptWhileAnotherDeliveryIsActive() {
        when(deliveries.findFirstByDriverIdAndStatusIn(anyLong(), any())).thenReturn(Optional.of(delivery(9L, 1L, null)));

        assertThatThrownBy(() -> service.accept(3L, 1L)).isInstanceOf(BusinessException.class);
    }

    @Test
    void concurrentAcceptBecomesConflict() {
        when(deliveries.findById(1L)).thenReturn(Optional.of(delivery(1L, 101L, null)));
        when(deliveries.saveAndFlush(any())).thenThrow(new ObjectOptimisticLockingFailureException(Delivery.class, 1L));

        assertThatThrownBy(() -> service.accept(3L, 1L)).isInstanceOf(ConflictException.class);
        verify(orderGateway, never()).assignDriver(any(), any());
    }

    @Test
    void orderServiceFailurePropagatesToRollBack() {
        when(deliveries.findById(1L)).thenReturn(Optional.of(delivery(1L, 101L, null)));
        doThrow(new ServiceUnavailableException("fora", null)).when(orderGateway).assignDriver(any(), any());

        assertThatThrownBy(() -> service.accept(3L, 1L)).isInstanceOf(ServiceUnavailableException.class);
    }

    @Test
    void pickupAndCompleteAdvanceOrder() {
        Delivery d = delivery(1L, 101L, null);
        when(deliveries.findById(1L)).thenReturn(Optional.of(d));
        service.accept(3L, 1L);

        service.pickUp(3L, 1L);
        verify(orderGateway).markOutForDelivery(101L, 3L);

        service.complete(3L, 1L);
        verify(orderGateway).markDelivered(101L, 3L);
        assertThat(d.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
    }

    @Test
    void onlyCustomerOrAssignedDriverCanTrack() {
        when(deliveries.findByOrderId(101L)).thenReturn(Optional.of(delivery(1L, 101L, null)));

        assertThat(service.track(new AuthenticatedUser(1L, "c@rota.dev", Role.CUSTOMER), 101L).orderId())
                .isEqualTo(101L);
        assertThatThrownBy(() -> service.track(new AuthenticatedUser(99L, "x@rota.dev", Role.CUSTOMER), 101L))
                .isInstanceOf(ForbiddenException.class);
    }
}
