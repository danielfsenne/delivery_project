package com.rota.common.events;

import java.time.Instant;
import java.util.UUID;

/**
 * Evento publicado no RabbitMQ para outros serviços.
 * O {@code eventId} é único e permite ao consumidor descartar entregas duplicadas.
 */
public interface IntegrationEvent {

    UUID eventId();

    Instant occurredAt();
}
