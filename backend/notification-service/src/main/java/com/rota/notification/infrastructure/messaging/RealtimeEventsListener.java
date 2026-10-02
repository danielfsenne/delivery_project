package com.rota.notification.infrastructure.messaging;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.events.DriverLocationUpdated;
import com.rota.common.events.OrderStatusChanged;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventQueues;
import com.rota.notification.application.RealtimeNotifier;
import com.rota.notification.application.port.RealtimeGateway;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

/**
 * Fila exclusiva desta instância (some quando ela cai): toda instância recebe todos os eventos
 * de tempo real e repassa aos usuários conectados nela. Diferente dos e-mails, aqui não há
 * competição entre consumidores.
 */
@Configuration
@RabbitListener(queues = "#{T(com.rota.notification.infrastructure.messaging.RealtimeEventsListener).QUEUE}")
public class RealtimeEventsListener {

    /** Nome único por instância. */
    public static final String QUEUE = "notification.realtime." + UUID.randomUUID();

    private final RealtimeNotifier notifier;

    public RealtimeEventsListener(RealtimeGateway gateway) {
        this.notifier = new RealtimeNotifier(gateway);
    }

    @Bean
    Declarables realtimeQueue() {
        return EventQueues.transientPerInstance(QUEUE, RotaEvents.ORDER_STATUS_CHANGED,
                RotaEvents.DELIVERY_STATUS_CHANGED, RotaEvents.DRIVER_LOCATION_UPDATED);
    }

    @RabbitHandler
    public void on(OrderStatusChanged event) {
        notifier.on(event);
    }

    @RabbitHandler
    public void on(DeliveryStatusChanged event) {
        notifier.on(event);
    }

    @RabbitHandler
    public void on(DriverLocationUpdated event) {
        notifier.on(event);
    }
}
