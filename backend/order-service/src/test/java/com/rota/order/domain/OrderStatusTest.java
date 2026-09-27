package com.rota.order.domain;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;

class OrderStatusTest {

    @ParameterizedTest
    @CsvSource({
            "CREATED, PAYMENT_PENDING",
            "PAYMENT_PENDING, PAID",
            "PAID, RESTAURANT_ACCEPTED",
            "RESTAURANT_ACCEPTED, PREPARING",
            "PREPARING, READY_FOR_PICKUP",
            "READY_FOR_PICKUP, OUT_FOR_DELIVERY",
            "OUT_FOR_DELIVERY, DELIVERED",
            "PAID, CANCELLED",
            "RESTAURANT_ACCEPTED, CANCELLED"
    })
    void shouldAllowHappyPathAndEarlyCancellation(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({
            "DELIVERED, PREPARING",
            "CREATED, PAID",
            "PAYMENT_PENDING, PREPARING",
            "PREPARING, CANCELLED",
            "OUT_FOR_DELIVERY, CANCELLED",
            "READY_FOR_PICKUP, PREPARING",
            "CANCELLED, CREATED"
    })
    void shouldRejectInvalidTransitions(OrderStatus from, OrderStatus to) {
        assertThat(from.canTransitionTo(to)).isFalse();
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"DELIVERED", "CANCELLED"})
    void finalStatusesHaveNoNextStatus(OrderStatus status) {
        assertThat(status.isFinal()).isTrue();
        assertThat(status.nextStatuses()).isEmpty();
    }

    @Test
    void noStatusCanTransitionToItself() {
        for (OrderStatus status : OrderStatus.values()) {
            assertThat(status.canTransitionTo(status)).as(status.name()).isFalse();
        }
    }
}
