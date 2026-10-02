package com.rota.notification.application;

import java.time.Instant;

/**
 * Mensagem enviada ao front pelo WebSocket. O {@code type} diz como interpretar o {@code payload}.
 */
public record RealtimeMessage(String type, Object payload) {

    /** Cliente: status do seu pedido mudou. */
    public static final String ORDER_STATUS = "order.status";

    /** Restaurante: um pedido do seu restaurante mudou (inclusive pedido novo pago). */
    public static final String RESTAURANT_ORDER = "restaurant.order";

    /** Entregadores: a lista de corridas disponíveis mudou. */
    public static final String DELIVERIES_CHANGED = "deliveries.changed";

    /** Cliente: nova posição do entregador do seu pedido. */
    public static final String DRIVER_LOCATION = "driver.location";

    public record OrderUpdate(Long orderId, Long restaurantId, String restaurantName, String previousStatus,
                              String status, Instant at) {
    }

    public record DeliveryUpdate(Long deliveryId, Long orderId, String status, Instant at) {
    }

    public record LocationUpdate(Long orderId, double latitude, double longitude, Instant at) {
    }
}
