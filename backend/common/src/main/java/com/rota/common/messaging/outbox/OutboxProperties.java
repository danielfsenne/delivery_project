package com.rota.common.messaging.outbox;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.time.Duration;

/**
 * @param enabled        liga o outbox (o serviço precisa da tabela {@code outbox_events})
 * @param batchSize      eventos lidos por transação do relay
 * @param confirmTimeout espera máxima pela confirmação do RabbitMQ
 * @param retentionDays  por quantos dias manter eventos já publicados (auditoria)
 */
@ConfigurationProperties(prefix = "rota.messaging.outbox")
public record OutboxProperties(
        @DefaultValue("false") boolean enabled,
        @DefaultValue("100") int batchSize,
        @DefaultValue("5s") Duration confirmTimeout,
        @DefaultValue("3") int retentionDays
) {
}
