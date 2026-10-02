package com.rota.notification.application;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.events.DriverLocationUpdated;
import com.rota.common.events.OrderStatusChanged;
import com.rota.notification.application.RealtimeMessage.DeliveryUpdate;
import com.rota.notification.application.RealtimeMessage.LocationUpdate;
import com.rota.notification.application.RealtimeMessage.OrderUpdate;
import com.rota.notification.application.port.RealtimeGateway;

/**
 * Decide quem recebe cada evento. Só identificadores e status vão pelo WebSocket;
 * o front busca os detalhes pela API, que aplica as regras de acesso.
 */
public class RealtimeNotifier {

    private final RealtimeGateway gateway;

    public RealtimeNotifier(RealtimeGateway gateway) {
        this.gateway = gateway;
    }

    public void on(OrderStatusChanged event) {
        OrderUpdate update = new OrderUpdate(event.orderId(), event.restaurantId(), event.restaurantName(),
                event.previousStatus(), event.status(), event.occurredAt());
        gateway.toUser(event.customerId(), new RealtimeMessage(RealtimeMessage.ORDER_STATUS, update));
        gateway.toUser(event.restaurantOwnerId(), new RealtimeMessage(RealtimeMessage.RESTAURANT_ORDER, update));
    }

    public void on(DeliveryStatusChanged event) {
        gateway.toDrivers(new RealtimeMessage(RealtimeMessage.DELIVERIES_CHANGED,
                new DeliveryUpdate(event.deliveryId(), event.orderId(), event.status(), event.occurredAt())));
    }

    public void on(DriverLocationUpdated event) {
        gateway.toUser(event.customerId(), new RealtimeMessage(RealtimeMessage.DRIVER_LOCATION,
                new LocationUpdate(event.orderId(), event.latitude(), event.longitude(), event.occurredAt())));
    }
}
