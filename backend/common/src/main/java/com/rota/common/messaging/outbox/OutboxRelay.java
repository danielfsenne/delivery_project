package com.rota.common.messaging.outbox;

import com.rota.common.events.RotaEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageBuilder;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Lê o outbox em ordem e publica no RabbitMQ, esperando a confirmação do broker
 * (publisher confirms) antes de marcar o evento como enviado.
 *
 * <ul>
 *   <li><b>Entrega pelo menos uma vez:</b> se o broker confirmar e o commit falhar, o evento
 *       sai de novo. Os consumidores são idempotentes por {@code eventId}.</li>
 *   <li><b>Ordem:</b> ao primeiro erro o lote para; o ciclo seguinte recomeça do mesmo evento.</li>
 *   <li><b>Várias instâncias:</b> {@code FOR UPDATE SKIP LOCKED} impede que duas publiquem o mesmo lote.</li>
 *   <li><b>Trace:</b> o envio continua o trace da requisição que gravou o evento ({@link OutboxTracing}).</li>
 * </ul>
 */
public class OutboxRelay {

    private static final Logger log = LoggerFactory.getLogger(OutboxRelay.class);

    private static final String SELECT_PENDING = """
            SELECT id, event_id, routing_key, event_type, payload, trace_parent
              FROM outbox_events
             WHERE published_at IS NULL
             ORDER BY id
             LIMIT ?
               FOR UPDATE SKIP LOCKED
            """;
    private static final String MARK_PUBLISHED = "UPDATE outbox_events SET published_at = now() WHERE id = ?";
    private static final String MARK_FAILED =
            "UPDATE outbox_events SET attempts = attempts + 1, last_error = ? WHERE id = ?";
    private static final String DELETE_OLD =
            "DELETE FROM outbox_events WHERE published_at < now() - make_interval(days => ?)";

    private static final RowMapper<PendingEvent> MAPPER = (rs, i) -> new PendingEvent(
            rs.getLong("id"), rs.getObject("event_id", UUID.class), rs.getString("routing_key"),
            rs.getString("event_type"), rs.getString("payload"), rs.getString("trace_parent"));

    private final JdbcTemplate jdbc;
    private final TransactionTemplate transaction;
    private final RabbitTemplate rabbit;
    private final int batchSize;
    private final Duration confirmTimeout;
    private final int retentionDays;
    private final OutboxTracing tracing;

    public OutboxRelay(JdbcTemplate jdbc, TransactionTemplate transaction, RabbitTemplate rabbit,
                       OutboxProperties properties) {
        this(jdbc, transaction, rabbit, properties, OutboxTracing.NOOP);
    }

    public OutboxRelay(JdbcTemplate jdbc, TransactionTemplate transaction, RabbitTemplate rabbit,
                       OutboxProperties properties, OutboxTracing tracing) {
        this.jdbc = jdbc;
        this.transaction = transaction;
        this.rabbit = rabbit;
        this.batchSize = properties.batchSize();
        this.confirmTimeout = properties.confirmTimeout();
        this.retentionDays = properties.retentionDays();
        this.tracing = tracing;
        if (!rabbit.getConnectionFactory().isPublisherConfirms()) {
            throw new IllegalStateException(
                    "O outbox exige spring.rabbitmq.publisher-confirm-type=correlated para confirmar cada envio");
        }
    }

    @Scheduled(fixedDelayString = "${rota.messaging.outbox.poll-interval-ms:500}")
    public void relay() {
        Integer sent;
        do {
            sent = transaction.execute(status -> publishBatch());
        } while (sent != null && sent == batchSize);
    }

    @Scheduled(fixedDelay = 1, initialDelay = 1, timeUnit = TimeUnit.HOURS)
    public void deletePublished() {
        int deleted = jdbc.update(DELETE_OLD, retentionDays);
        if (deleted > 0) {
            log.info("Outbox: {} eventos já publicados removidos", deleted);
        }
    }

    int publishBatch() {
        List<PendingEvent> pending = jdbc.query(SELECT_PENDING, MAPPER, batchSize);
        int sent = 0;
        for (PendingEvent event : pending) {
            try {
                tracing.inTrace(event.traceParent(), "outbox " + event.routingKey(), () -> send(event));
            } catch (Exception e) {
                jdbc.update(MARK_FAILED, truncate(e.getMessage()), event.id());
                log.warn("Outbox: falha ao publicar {} ({}); nova tentativa no próximo ciclo: {}",
                        event.routingKey(), event.eventId(), e.getMessage());
                break;
            }
            jdbc.update(MARK_PUBLISHED, event.id());
            sent++;
        }
        return sent;
    }

    private void send(PendingEvent event) throws Exception {
        Message message = MessageBuilder.withBody(event.payload().getBytes(StandardCharsets.UTF_8))
                .setContentType(MessageProperties.CONTENT_TYPE_JSON)
                .setContentEncoding(StandardCharsets.UTF_8.name())
                .setMessageId(event.eventId().toString())
                .setDeliveryMode(MessageDeliveryMode.PERSISTENT)
                .setHeader("__TypeId__", event.eventType())
                .build();
        CorrelationData correlation = new CorrelationData(event.eventId().toString());
        rabbit.send(RotaEvents.EXCHANGE, event.routingKey(), message, correlation);

        CorrelationData.Confirm confirm = correlation.getFuture()
                .get(confirmTimeout.toMillis(), TimeUnit.MILLISECONDS);
        if (!confirm.isAck()) {
            throw new IllegalStateException("Broker recusou a mensagem: " + confirm.getReason());
        }
    }

    private static String truncate(String message) {
        if (message == null) {
            return null;
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }

    record PendingEvent(long id, UUID eventId, String routingKey, String eventType, String payload,
                        String traceParent) {
    }
}
