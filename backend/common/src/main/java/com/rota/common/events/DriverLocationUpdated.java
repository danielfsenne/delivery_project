package com.rota.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Posição do entregador em uma entrega ativa. Evento efêmero: publicado direto, sem outbox,
 * porque perder uma posição não importa (a próxima chega em segundos).
 */
public record DriverLocationUpdated(
        UUID eventId,
        Long driverId,
        Long orderId,
        Long customerId,
        double latitude,
        double longitude,
        Instant occurredAt
) implements IntegrationEvent {
}
