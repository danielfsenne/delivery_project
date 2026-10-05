package com.rota.order.infrastructure.metrics;

import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.OrderStatusChangedEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.EnumMap;
import java.util.Map;

/**
 * Métricas de negócio para o Grafana: pedidos por status e valor dos pedidos pagos.
 * Só conta depois do commit, para um rollback não inflar os números.
 */
@Component
public class OrderMetrics {

    private final Map<OrderStatus, Counter> statusChanges = new EnumMap<>(OrderStatus.class);
    private final DistributionSummary paidAmount;

    public OrderMetrics(MeterRegistry registry) {
        // Todos os contadores nascem zerados: o Prometheus só calcula increase() a partir
        // da segunda amostra, então um contador criado no primeiro pedido perderia esse pedido.
        for (OrderStatus status : OrderStatus.values()) {
            statusChanges.put(status, Counter.builder("rota.orders.status.changes")
                    .description("Pedidos que entraram em cada status")
                    .tag("status", status.name())
                    .register(registry));
        }
        this.paidAmount = DistributionSummary.builder("rota.orders.paid.amount")
                .description("Valor dos pedidos pagos, em reais")
                .register(registry);
    }

    @TransactionalEventListener(fallbackExecution = true)
    public void on(OrderStatusChangedEvent event) {
        statusChanges.get(event.current()).increment();
        if (event.current() == OrderStatus.PAID) {
            paidAmount.record(event.order().getTotal().doubleValue());
        }
    }
}
