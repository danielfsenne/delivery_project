package com.rota.order.application;

import com.rota.common.exception.ForbiddenException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrderServiceTest {

    private final OrderRepository orders = mock(OrderRepository.class);
    private final OrderService service = new OrderService(orders, new OrderAccessPolicy(), Clock.systemUTC());
    private final AuthenticatedUser owner = new AuthenticatedUser(2L, "r@rota.dev", Role.RESTAURANT);

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .customer(1L)
                .restaurant(10L, "Burger House", 2L)
                .paymentMethod(PaymentMethod.PIX)
                .item(new OrderItem(1L, "X", null, null, BigDecimal.TEN, 1))
                .build();
        for (OrderStatus s : new OrderStatus[]{OrderStatus.PAYMENT_PENDING, OrderStatus.PAID,
                OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.PREPARING}) {
            order.transitionTo(s, null, null, Instant.now());
        }
        when(orders.findById(50L)).thenReturn(Optional.of(order));
        when(orders.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void restaurantMarksOrderReady() {
        var response = service.changeStatus(owner, 50L, OrderStatus.READY_FOR_PICKUP, null);

        assertThat(response.status()).isEqualTo(OrderStatus.READY_FOR_PICKUP);
        verify(orders).saveAndFlush(order);
    }

    @Test
    void driverCannotChangeStatusDirectly() {
        order.assignDriver(3L);
        AuthenticatedUser driver = new AuthenticatedUser(3L, "d@rota.dev", Role.DRIVER);

        assertThatThrownBy(() -> service.changeStatus(driver, 50L, OrderStatus.READY_FOR_PICKUP, null))
                .isInstanceOf(ForbiddenException.class);
        verify(orders, never()).saveAndFlush(any());
    }

    @Test
    void deliveryProgressRecordsActingDriverInHistory() {
        order.transitionTo(OrderStatus.READY_FOR_PICKUP, 2L, null, Instant.now());

        service.advanceByDelivery(50L, OrderStatus.OUT_FOR_DELIVERY, 3L);

        assertThat(order.getStatus()).isEqualTo(OrderStatus.OUT_FOR_DELIVERY);
        assertThat(order.getHistory().getLast().getUserId()).isEqualTo(3L);
    }

    @Test
    void deliveryProgressIsIdempotent() {
        order.transitionTo(OrderStatus.READY_FOR_PICKUP, 2L, null, Instant.now());
        service.advanceByDelivery(50L, OrderStatus.OUT_FOR_DELIVERY, 3L);
        int historySize = order.getHistory().size();

        service.advanceByDelivery(50L, OrderStatus.OUT_FOR_DELIVERY, 3L);

        assertThat(order.getHistory()).hasSize(historySize);
    }
}
