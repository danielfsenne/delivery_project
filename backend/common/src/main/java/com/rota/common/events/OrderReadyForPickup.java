package com.rota.common.events;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Tudo o que o delivery-service precisa para abrir a corrida sem consultar o order-service.
 */
public record OrderReadyForPickup(
        UUID eventId,
        Long orderId,
        Long customerId,
        Long restaurantId,
        String restaurantName,
        String pickupAddress,
        Double pickupLatitude,
        Double pickupLongitude,
        String dropoffAddress,
        Double dropoffLatitude,
        Double dropoffLongitude,
        BigDecimal deliveryFee,
        Instant occurredAt
) implements IntegrationEvent {
}
