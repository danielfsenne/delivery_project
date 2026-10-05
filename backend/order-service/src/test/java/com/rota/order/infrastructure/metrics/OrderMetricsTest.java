package com.rota.order.infrastructure.metrics;

import com.rota.order.domain.Order;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.OrderStatusChangedEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrderMetricsTest {

    private final SimpleMeterRegistry registry = new SimpleMeterRegistry();
    private final OrderMetrics metrics = new OrderMetrics(registry);

    @Test
    void contaPedidosPorStatusERegistraValorPago() {
        Order order = mock(Order.class);
        when(order.getTotal()).thenReturn(new BigDecimal("87.50"));

        metrics.on(new OrderStatusChangedEvent(order, null, OrderStatus.PAYMENT_PENDING, null, Instant.now()));
        metrics.on(new OrderStatusChangedEvent(order, OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, null, Instant.now()));

        assertThat(registry.get("rota.orders.status.changes").tag("status", "PAID").counter().count()).isEqualTo(1);
        assertThat(registry.get("rota.orders.status.changes").tag("status", "PAYMENT_PENDING").counter().count())
                .isEqualTo(1);
        assertThat(registry.get("rota.orders.paid.amount").summary().totalAmount()).isEqualTo(87.5);
    }
}
