package com.rota.order.application;

import com.rota.common.events.OrderReadyForPickup;
import com.rota.common.events.OrderStatusChanged;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventPublisher;
import com.rota.order.domain.DeliveryAddress;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.OrderStatusChangedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Traduz eventos de domínio do pedido em eventos de integração. Roda de forma síncrona,
 * dentro da transação que salvou o pedido, então o evento vai para o outbox junto com a mudança.
 */
@Component
public class OrderIntegrationEvents {

    private final EventPublisher publisher;

    public OrderIntegrationEvents(EventPublisher publisher) {
        this.publisher = publisher;
    }

    @EventListener
    @Transactional(propagation = Propagation.MANDATORY)
    public void on(OrderStatusChangedEvent event) {
        Order order = event.order();
        publisher.publish(RotaEvents.ORDER_STATUS_CHANGED, new OrderStatusChanged(
                UUID.randomUUID(), order.getId(), order.getCustomerId(), order.getCustomerEmail(),
                order.getRestaurantId(), order.getRestaurantName(), order.getRestaurantOwnerId(),
                order.getDriverId(), event.previous() == null ? null : event.previous().name(),
                event.current().name(), order.getTotal(), event.reason(), event.occurredAt()));

        if (event.current() == OrderStatus.READY_FOR_PICKUP) {
            DeliveryAddress dropoff = order.getDeliveryAddress();
            publisher.publish(RotaEvents.ORDER_READY_FOR_PICKUP, new OrderReadyForPickup(
                    UUID.randomUUID(), order.getId(), order.getCustomerId(), order.getRestaurantId(),
                    order.getRestaurantName(), order.getPickupAddress(), order.getPickupLatitude(),
                    order.getPickupLongitude(), dropoff.formatted(), dropoff.latitude(), dropoff.longitude(),
                    order.getDeliveryFee(), event.occurredAt()));
        }
    }
}
