package com.rota.common.messaging.outbox;

import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.outbox.OutboxRelay.PendingEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.support.TransactionTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OutboxRelayTest {

    private final JdbcTemplate jdbc = mock(JdbcTemplate.class);
    private final RabbitTemplate rabbit = mock(RabbitTemplate.class);
    private final ConnectionFactory connectionFactory = mock(ConnectionFactory.class);
    private final OutboxProperties properties = new OutboxProperties(true, 10, Duration.ofSeconds(1), 3);

    private final PendingEvent first = new PendingEvent(1, UUID.randomUUID(), "order.status-changed",
            "com.rota.common.events.OrderStatusChanged", "{\"orderId\":1}", null);
    private final PendingEvent second = new PendingEvent(2, UUID.randomUUID(), "order.status-changed",
            "com.rota.common.events.OrderStatusChanged", "{\"orderId\":2}", null);

    private OutboxRelay relay;

    @BeforeEach
    void setUp() {
        when(rabbit.getConnectionFactory()).thenReturn(connectionFactory);
        when(connectionFactory.isPublisherConfirms()).thenReturn(true);
        relay = new OutboxRelay(jdbc, mock(TransactionTemplate.class), rabbit, properties);
    }

    @Test
    void publicaEmOrdemEMarcaComoEnviado() {
        pending(first, second);
        brokerAnswers(true, true);

        assertThat(relay.publishBatch()).isEqualTo(2);

        verify(rabbit, times(2)).send(eq(RotaEvents.EXCHANGE), eq("order.status-changed"),
                any(Message.class), any(CorrelationData.class));
        verify(jdbc).update(startsWith("UPDATE outbox_events SET published_at"), eq(1L));
        verify(jdbc).update(startsWith("UPDATE outbox_events SET published_at"), eq(2L));
    }

    @Test
    void mensagemLevaIdDoEventoETipoParaOConsumidor() {
        pending(first);
        doAnswer(invocation -> {
            Message message = invocation.getArgument(2);
            assertThat(message.getMessageProperties().getMessageId()).isEqualTo(first.eventId().toString());
            assertThat(message.getMessageProperties().getContentType()).isEqualTo("application/json");
            assertThat(message.getMessageProperties().<String>getHeader("__TypeId__")).isEqualTo(first.eventType());
            assertThat(new String(message.getBody(), StandardCharsets.UTF_8)).isEqualTo(first.payload());
            CorrelationData correlation = invocation.getArgument(3);
            correlation.getFuture().complete(new CorrelationData.Confirm(true, null));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));

        assertThat(relay.publishBatch()).isEqualTo(1);
    }

    @Test
    void paraNoPrimeiroErroParaPreservarAOrdem() {
        pending(first, second);
        brokerAnswers(false, true);

        assertThat(relay.publishBatch()).isZero();

        verify(jdbc).update(startsWith("UPDATE outbox_events SET attempts"), anyString(), eq(1L));
        verify(jdbc, never()).update(startsWith("UPDATE outbox_events SET published_at"), eq(2L));
    }

    @Test
    void exigePublisherConfirms() {
        when(connectionFactory.isPublisherConfirms()).thenReturn(false);

        assertThatThrownBy(() -> new OutboxRelay(jdbc, mock(TransactionTemplate.class), rabbit, properties))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("publisher-confirm-type");
    }

    @SuppressWarnings("unchecked")
    private void pending(PendingEvent... events) {
        when(jdbc.query(startsWith("SELECT id, event_id"), any(RowMapper.class), anyInt()))
                .thenReturn(List.of(events));
    }

    private void brokerAnswers(boolean... acks) {
        int[] call = {0};
        doAnswer(invocation -> {
            CorrelationData correlation = invocation.getArgument(3);
            boolean ack = acks[call[0]++];
            correlation.getFuture().complete(new CorrelationData.Confirm(ack, ack ? null : "nack"));
            return null;
        }).when(rabbit).send(anyString(), anyString(), any(Message.class), any(CorrelationData.class));
    }
}
