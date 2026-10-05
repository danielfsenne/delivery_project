package com.rota.common.observability;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.StandardEnvironment;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityDefaultsTest {

    @Test
    void preencheOsPadroesResolvendoONomeDoServico() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("app",
                Map.of("spring.application.name", "order-service")));

        new ObservabilityDefaults().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("management.metrics.tags.application")).isEqualTo("order-service");
    }

    @Test
    void configuracaoDoServicoTemPrioridade() {
        StandardEnvironment environment = new StandardEnvironment();
        environment.getPropertySources().addFirst(new MapPropertySource("app",
                Map.of("management.metrics.distribution.percentiles-histogram.http.server.requests", "false")));

        new ObservabilityDefaults().postProcessEnvironment(environment, new SpringApplication());

        assertThat(environment.getProperty("management.metrics.distribution.percentiles-histogram.http.server.requests"))
                .isEqualTo("false");
    }
}
