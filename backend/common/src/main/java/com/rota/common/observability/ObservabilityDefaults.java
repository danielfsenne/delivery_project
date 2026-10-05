package com.rota.common.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

import static java.util.Map.entry;

/**
 * Padrões de observabilidade iguais em todos os serviços. Entram com a menor precedência:
 * qualquer serviço pode sobrescrever no próprio application.yml ou por variável de ambiente.
 */
public class ObservabilityDefaults implements EnvironmentPostProcessor {

    static final String SOURCE_NAME = "rotaObservabilityDefaults";

    static final Map<String, Object> DEFAULTS = Map.ofEntries(
            // Toda métrica sai com o nome do serviço, para filtrar e agrupar no Grafana.
            entry("management.metrics.tags.application", "${spring.application.name}"),
            // Buckets de latência das requisições HTTP, para calcular p95/p99 no Prometheus.
            entry("management.metrics.distribution.percentiles-histogram.http.server.requests", "true"),
            // Ambiente de demonstração: todo trace é guardado. Em produção, algo como 0.1.
            entry("management.tracing.sampling.probability", "1.0"),
            entry("management.otlp.tracing.endpoint",
                    "${OTEL_EXPORTER_OTLP_ENDPOINT:http://localhost:4318}/v1/traces"),
            // O contexto do trace viaja nos headers das mensagens do RabbitMQ.
            entry("spring.rabbitmq.template.observation-enabled", "true"),
            entry("spring.rabbitmq.listener.simple.observation-enabled", "true")
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, DEFAULTS));
    }
}
