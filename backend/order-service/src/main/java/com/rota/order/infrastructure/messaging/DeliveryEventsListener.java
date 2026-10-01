package com.rota.order.infrastructure.messaging;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventQueues;
import com.rota.order.application.DeliveryProgressService;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Uma fila com um único consumidor preserva a ordem dos eventos de cada corrida
 * (aceita, retirada, entregue).
 */
@Configuration
public class DeliveryEventsListener {

    static final String DELIVERY_STATUS_QUEUE = "order.delivery-status";

    private final DeliveryProgressService deliveryProgress;

    public DeliveryEventsListener(DeliveryProgressService deliveryProgress) {
        this.deliveryProgress = deliveryProgress;
    }

    @Bean
    Declarables deliveryStatusQueue() {
        return EventQueues.durable(DELIVERY_STATUS_QUEUE, RotaEvents.DELIVERY_STATUS_CHANGED);
    }

    @RabbitListener(queues = DELIVERY_STATUS_QUEUE)
    public void onDeliveryStatusChanged(DeliveryStatusChanged event) {
        deliveryProgress.apply(event);
    }
}
