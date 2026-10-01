package com.rota.common.messaging;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

/**
 * Consumidor idempotente: registra cada evento processado na tabela {@code processed_events},
 * na mesma transação do efeito. Uma reentrega do mesmo evento é reconhecida e ignorada.
 */
public class ProcessedEvents {

    private static final String INSERT = """
            INSERT INTO processed_events (event_id, consumer, processed_at)
            VALUES (?, ?, now())
            ON CONFLICT DO NOTHING
            """;

    private final JdbcTemplate jdbc;

    public ProcessedEvents(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    /**
     * @return {@code true} na primeira vez que o consumidor vê o evento; {@code false} se for repetido
     */
    public boolean firstDelivery(UUID eventId, String consumer) {
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("A verificação de duplicidade precisa estar na transação do efeito");
        }
        return jdbc.update(INSERT, eventId, consumer) == 1;
    }
}
