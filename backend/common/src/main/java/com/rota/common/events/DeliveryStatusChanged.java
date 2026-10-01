package com.rota.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Status da corrida: WAITING_DRIVER, ASSIGNED, PICKED_UP ou DELIVERED.
 */
public record DeliveryStatusChanged(
        UUID eventId,
        Long deliveryId,
        Long orderId,
        Long customerId,
        Long driverId,
        String status,
        Instant occurredAt
) implements IntegrationEvent {
}
