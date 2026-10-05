package com.rota.common.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.rota.common.events.ReviewCreated;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class OutboxEventPublisherTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final OutboxEventPublisher publisher =
            new OutboxEventPublisher(jdbc, new ObjectMapper().registerModule(new JavaTimeModule()));

    private final ReviewCreated event = new ReviewCreated(UUID.randomUUID(), 10L, 20L, 3L, 4L, 5, 4,
            Instant.parse("2026-10-01T12:00:00Z"));

    @AfterEach
    void limpaTransacao() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void foraDeTransacaoRecusa() {
        assertThatThrownBy(() -> publisher.publish("review.created", event))
                .isInstanceOf(IllegalStateException.class);
        verifyNoInteractions(jdbc);
    }

    @Test
    void dentroDaTransacaoGravaNoOutbox() {
        TransactionSynchronizationManager.setActualTransactionActive(true);

        publisher.publish("review.created", event);

        verify(jdbc).update(anyString(), eq(event.eventId()), eq("review.created"),
                eq(ReviewCreated.class.getName()), contains("\"foodRating\":5"),
                eq(Timestamp.from(event.occurredAt())), isNull());
    }
}
