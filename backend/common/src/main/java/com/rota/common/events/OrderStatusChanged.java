package com.rota.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Status como texto (e não o enum do order-service) para não acoplar os consumidores
 * ao modelo interno de pedidos.
 */
public record OrderStatusChanged(
        UUID eventId,
        Long orderId,
        Long customerId,
        String customerEmail,
        Long restaurantId,
        String restaurantName,
        Long restaurantOwnerId,
        Long driverId,
        String previousStatus,
        String status,
        BigDecimal total,
        String reason,
        Instant occurredAt
) implements IntegrationEvent {
}
