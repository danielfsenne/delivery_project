package com.rota.common.observability;

import io.micrometer.observation.Observation;
import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.observation.ClientRequestObservationContext;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.net.URI;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;

class ObservabilityAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(ObservabilityAutoConfiguration.class));

    @Test
    void ignoraActuatorEurekaEPollingDoOutbox() {
        runner.run(context -> {
            Collection<ObservationPredicate> predicates = context.getBeansOfType(ObservationPredicate.class).values();

            assertThat(allow(predicates, "http.server.requests", server("/actuator/prometheus"))).isFalse();
            assertThat(allow(predicates, "http.client.requests",
                    client("http://10.0.0.2:8761/eureka/apps/delta"))).isFalse();
            assertThat(allow(predicates, "tasks.scheduled.execution", new Observation.Context())).isFalse();
            assertThat(allow(predicates, "spring.security.filterchains", new Observation.Context())).isFalse();
        });
    }

    @Test
    void mantemRequisicoesDeNegocio() {
        runner.run(context -> {
            Collection<ObservationPredicate> predicates = context.getBeansOfType(ObservationPredicate.class).values();

            assertThat(allow(predicates, "http.server.requests", server("/orders/42"))).isTrue();
            assertThat(allow(predicates, "http.client.requests",
                    client("http://10.0.0.3:8184/payments"))).isTrue();

            Observation.Context security = new Observation.Context();
            security.setParentObservation(Observation.start("http.server.requests", ObservationRegistry.create()));
            assertThat(allow(predicates, "spring.security.filterchains", security)).isTrue();
        });
    }

    private static boolean allow(Collection<ObservationPredicate> predicates, String name, Observation.Context ctx) {
        return predicates.stream().allMatch(p -> p.test(name, ctx));
    }

    private static ServerRequestObservationContext server(String uri) {
        return new ServerRequestObservationContext(new MockHttpServletRequest("GET", uri), new MockHttpServletResponse());
    }

    private static ClientRequestObservationContext client(String uri) {
        return new ClientRequestObservationContext(new MockClientHttpRequest(HttpMethod.GET, URI.create(uri)));
    }
}
