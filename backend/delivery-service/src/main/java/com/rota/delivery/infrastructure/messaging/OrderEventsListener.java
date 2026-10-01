package com.rota.delivery.infrastructure.messaging;

import com.rota.common.events.OrderReadyForPickup;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventQueues;
import com.rota.delivery.application.DeliveryService;
import com.rota.delivery.interfaces.rest.dto.DeliveryDtos.CreateDeliveryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Pedido pronto no restaurante: abre a corrida. A criação é idempotente por pedido,
 * então uma reentrega do evento não gera corrida duplicada.
 */
@Configuration
public class OrderEventsListener {

    static final String ORDER_READY_QUEUE = "delivery.order-ready";

    private static final Logger log = LoggerFactory.getLogger(OrderEventsListener.class);

    private final DeliveryService deliveryService;

    public OrderEventsListener(DeliveryService deliveryService) {
        this.deliveryService = deliveryService;
    }

    @Bean
    Declarables orderReadyQueue() {
        return EventQueues.durable(ORDER_READY_QUEUE, RotaEvents.ORDER_READY_FOR_PICKUP);
    }

    @RabbitListener(queues = ORDER_READY_QUEUE)
    public void onOrderReady(OrderReadyForPickup event) {
        log.info("Pedido {} pronto: abrindo corrida", event.orderId());
        deliveryService.create(new CreateDeliveryRequest(event.orderId(), event.customerId(),
                event.restaurantId(), event.restaurantName(), event.pickupAddress(), event.pickupLatitude(),
                event.pickupLongitude(), event.dropoffAddress(), event.dropoffLatitude(),
                event.dropoffLongitude(), event.deliveryFee()));
    }
}
