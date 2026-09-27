package com.rota.order.domain;

import com.rota.common.exception.ConflictException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private Order newOrder() {
        return Order.builder()
                .customer(1L)
                .restaurant(10L, "Burger House", 2L)
                .paymentMethod(PaymentMethod.PIX)
                .deliveryAddress(new DeliveryAddress("Rua A", "1", null, "Centro", "Franca", "SP", "14400-000",
                        null, null))
                .deliveryFee(new BigDecimal("5.99"))
                .discount(new BigDecimal("3.00"))
                .item(new OrderItem(100L, "X-Bacon", "Bacon extra", null, new BigDecimal("37.90"), 2))
                .item(new OrderItem(200L, "Coca-Cola", null, null, new BigDecimal("6.50"), 1))
                .createdAt(NOW)
                .build();
    }

    @Test
    void shouldCalculateTotalsFromItems() {
        Order order = newOrder();

        assertThat(order.getSubtotal()).isEqualByComparingTo("82.30");
        assertThat(order.getTotal()).isEqualByComparingTo("85.29");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
    }

    @Test
    void shouldRecordCreationInHistory() {
        Order order = newOrder();

        assertThat(order.getHistory()).singleElement().satisfies(h -> {
            assertThat(h.getOldStatus()).isNull();
            assertThat(h.getNewStatus()).isEqualTo(OrderStatus.CREATED);
            assertThat(h.getEvent()).isEqualTo("ORDER_CREATED");
            assertThat(h.getUserId()).isEqualTo(1L);
        });
    }

    @Test
    void shouldRecordEachTransitionInHistory() {
        Order order = newOrder();

        order.transitionTo(OrderStatus.PAYMENT_PENDING, null, null, NOW.plusSeconds(1));
        order.transitionTo(OrderStatus.PAID, null, null, NOW.plusSeconds(2));
        order.transitionTo(OrderStatus.RESTAURANT_ACCEPTED, 2L, null, NOW.plusSeconds(3));

        assertThat(order.getStatus()).isEqualTo(OrderStatus.RESTAURANT_ACCEPTED);
        assertThat(order.getHistory())
                .extracting(OrderHistory::getNewStatus)
                .containsExactly(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING, OrderStatus.PAID,
                        OrderStatus.RESTAURANT_ACCEPTED);
        assertThat(order.getUpdatedAt()).isEqualTo(NOW.plusSeconds(3));
    }

    @Test
    void shouldRejectInvalidTransitionAndKeepState() {
        Order order = newOrder();

        assertThatThrownBy(() -> order.transitionTo(OrderStatus.DELIVERED, 1L, null, NOW))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CREATED -> DELIVERED");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.CREATED);
        assertThat(order.getHistory()).hasSize(1);
    }

    @Test
    void totalShouldNeverBeNegative() {
        Order order = Order.builder()
                .customer(1L)
                .restaurant(10L, "X", 2L)
                .paymentMethod(PaymentMethod.CASH)
                .discount(new BigDecimal("100"))
                .item(new OrderItem(1L, "Água", null, null, new BigDecimal("3.00"), 1))
                .build();

        assertThat(order.getTotal()).isEqualByComparingTo("0.00");
    }
}
