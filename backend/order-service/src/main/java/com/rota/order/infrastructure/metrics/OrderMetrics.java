package com.rota.order.infrastructure.metrics;

import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.OrderStatusChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Métricas de negócio para o Grafana: pedidos por status e valor dos pedidos pagos.
 * Só conta depois do commit, para um rollback não inflar os números.
 */
@Component
public class OrderMetrics {

    private final MeterRegistry registry;
    private final DistributionSummary paidAmount;

    public OrderMetrics(MeterRegistry registry) {
        this.registry = registry;
        this.paidAmount = DistributionSummary.builder("rota.orders.paid.amount")
                .description("Valor dos pedidos pagos, em reais")
                .register(registry);
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void on(OrderStatusChangedEvent event) {
        Counter.builder("rota.orders.status.changes")
                .description("Pedidos que entraram em cada status")
                .tag("status", event.current().name())
                .register(registry)
                .increment();

        if (event.current() == OrderStatus.PAID) {
            paidAmount.record(event.order().getTotal().doubleValue());
        }
    }
}
