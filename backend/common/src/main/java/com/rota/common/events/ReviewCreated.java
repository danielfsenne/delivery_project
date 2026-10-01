package com.rota.common.events;

import java.time.Instant;
import java.util.UUID;

public record ReviewCreated(
        UUID eventId,
        Long reviewId,
        Long orderId,
        Long restaurantId,
        Long driverId,
        int foodRating,
        Integer deliveryRating,
        Instant occurredAt
) implements IntegrationEvent {
}
