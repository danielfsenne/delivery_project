package com.rota.common.observability;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

import java.util.Map;

/**
 * Padrões de observabilidade iguais em todos os serviços. Entram com a menor precedência:
 * qualquer serviço pode sobrescrever no próprio application.yml ou por variável de ambiente.
 */
public class ObservabilityDefaults implements EnvironmentPostProcessor {

    static final String SOURCE_NAME = "rotaObservabilityDefaults";

    static final Map<String, Object> DEFAULTS = Map.of(
            // Toda métrica sai com o nome do serviço, para filtrar e agrupar no Grafana.
            "management.metrics.tags.application", "${spring.application.name}",
            // Buckets de latência das requisições HTTP, para calcular p95/p99 no Prometheus.
            "management.metrics.distribution.percentiles-histogram.http.server.requests", "true"
    );

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        environment.getPropertySources().addLast(new MapPropertySource(SOURCE_NAME, DEFAULTS));
    }
}
