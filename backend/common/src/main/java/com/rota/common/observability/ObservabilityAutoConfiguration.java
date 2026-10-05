package com.rota.common.observability;

import io.micrometer.observation.ObservationPredicate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.observation.ClientRequestObservationContext;

/**
 * Tira do tracing (e das métricas HTTP) o tráfego de infraestrutura: o Prometheus lendo
 * {@code /actuator} a cada poucos segundos, os heartbeats do Eureka e o polling do outbox.
 * Sem isso, os traces de verdade se perdem no meio de milhares de traces vazios.
 */
@AutoConfiguration
@ConditionalOnClass(ObservationPredicate.class)
public class ObservabilityAutoConfiguration {

    static final String ACTUATOR = "/actuator";

    @Bean
    ObservationPredicate ignoreScheduledTasks() {
        return (name, context) -> !"tasks.scheduled.execution".equals(name);
    }

    @Bean
    @ConditionalOnClass(ClientRequestObservationContext.class)
    ObservationPredicate ignoreEurekaCalls() {
        return (name, context) -> !(context instanceof ClientRequestObservationContext client
                && client.getCarrier() != null
                && client.getCarrier().getURI().getPath().contains("/eureka/"));
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.SERVLET)
    static class Servlet {

        @Bean
        ObservationPredicate ignoreActuatorRequests() {
            return (name, context) -> !(context
                    instanceof org.springframework.http.server.observation.ServerRequestObservationContext request
                    && request.getCarrier().getRequestURI().startsWith(ACTUATOR));
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnWebApplication(type = Type.REACTIVE)
    static class Reactive {

        @Bean
        ObservationPredicate ignoreActuatorRequests() {
            return (name, context) -> !(context
                    instanceof org.springframework.http.server.reactive.observation.ServerRequestObservationContext request
                    && request.getCarrier().getPath().value().startsWith(ACTUATOR));
        }
    }
}
