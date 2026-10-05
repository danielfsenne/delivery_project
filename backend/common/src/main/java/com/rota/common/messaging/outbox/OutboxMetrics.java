package com.rota.common.messaging.outbox;

import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Saúde do outbox no Prometheus: quantos eventos esperam publicação e há quanto tempo
 * o mais antigo espera. Fila crescendo com idade alta indica RabbitMQ fora ou relay travado.
 * Consultado a cada coleta do Prometheus, usando o índice parcial de pendentes.
 */
public class OutboxMetrics implements MeterBinder {

    private static final String PENDING = "SELECT count(*) FROM outbox_events WHERE published_at IS NULL";
    private static final String OLDEST_AGE = """
            SELECT coalesce(extract(epoch FROM now() - min(created_at)), 0)
              FROM outbox_events
             WHERE published_at IS NULL
            """;

    private final JdbcTemplate jdbc;

    public OutboxMetrics(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public void bindTo(MeterRegistry registry) {
        Gauge.builder("rota.outbox.pending", jdbc, j -> query(j, PENDING))
                .description("Eventos gravados no outbox e ainda não publicados")
                .register(registry);
        Gauge.builder("rota.outbox.oldest.age", jdbc, j -> query(j, OLDEST_AGE))
                .description("Tempo de espera do evento pendente mais antigo")
                .baseUnit("seconds")
                .register(registry);
    }

    private static double query(JdbcTemplate jdbc, String sql) {
        Number value = jdbc.queryForObject(sql, Number.class);
        return value == null ? 0 : value.doubleValue();
    }
}
