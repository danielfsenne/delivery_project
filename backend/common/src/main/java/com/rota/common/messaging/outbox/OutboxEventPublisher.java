package com.rota.common.messaging.outbox;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.events.IntegrationEvent;
import com.rota.common.messaging.EventPublisher;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;

/**
 * Transactional Outbox: o evento é gravado na tabela {@code outbox_events} junto com a
 * mudança de negócio, e o {@link OutboxRelay} o envia ao RabbitMQ depois.
 * O JdbcTemplate participa da mesma transação JPA (mesmo DataSource).
 */
public class OutboxEventPublisher implements EventPublisher {

    private static final String INSERT = """
            INSERT INTO outbox_events (event_id, routing_key, event_type, payload, created_at)
            VALUES (?, ?, ?, ?, ?)
            """;

    private final JdbcTemplate jdbc;
    private final ObjectMapper objectMapper;

    public OutboxEventPublisher(JdbcTemplate jdbc, ObjectMapper objectMapper) {
        this.jdbc = jdbc;
        this.objectMapper = objectMapper;
    }

    @Override
    public void publish(String routingKey, IntegrationEvent event) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("Eventos devem ser publicados dentro da transação da mudança");
        }
        jdbc.update(INSERT, event.eventId(), routingKey, event.getClass().getName(), toJson(event),
                Timestamp.from(event.occurredAt()));
    }

    private String toJson(IntegrationEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Evento não serializável: " + event.getClass().getName(), e);
        }
    }
}
