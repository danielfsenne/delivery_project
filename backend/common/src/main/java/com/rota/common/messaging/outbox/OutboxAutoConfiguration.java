package com.rota.common.messaging.outbox;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.messaging.EventPublisher;
import com.rota.common.messaging.ProcessedEvents;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.propagation.Propagator;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.JdbcTemplateAutoConfiguration;
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.support.TransactionTemplate;

@AutoConfiguration(after = {RabbitAutoConfiguration.class, JdbcTemplateAutoConfiguration.class,
        TransactionAutoConfiguration.class})
@ConditionalOnClass({RabbitTemplate.class, JdbcTemplate.class})
@EnableConfigurationProperties(OutboxProperties.class)
public class OutboxAutoConfiguration {

    /** Disponível em todo serviço com banco e RabbitMQ (requer a tabela {@code processed_events}). */
    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    ProcessedEvents processedEvents(JdbcTemplate jdbc) {
        return new ProcessedEvents(jdbc);
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "rota.messaging.outbox", name = "enabled", havingValue = "true")
    @EnableScheduling
    static class Outbox {

        @Bean
        @ConditionalOnMissingBean
        OutboxTracing outboxTracing(ObjectProvider<Tracer> tracer, ObjectProvider<Propagator> propagator) {
            Tracer t = tracer.getIfAvailable();
            Propagator p = propagator.getIfAvailable();
            return t != null && p != null ? new OutboxTracing(t, p) : OutboxTracing.NOOP;
        }

        @Bean
        @ConditionalOnMissingBean
        EventPublisher outboxEventPublisher(JdbcTemplate jdbc, ObjectMapper objectMapper, OutboxTracing tracing) {
            return new OutboxEventPublisher(jdbc, objectMapper, tracing);
        }

        @Bean
        OutboxRelay outboxRelay(JdbcTemplate jdbc, TransactionTemplate transaction, RabbitTemplate rabbit,
                                OutboxProperties properties, OutboxTracing tracing) {
            return new OutboxRelay(jdbc, transaction, rabbit, properties, tracing);
        }

        @Bean
        OutboxMetrics outboxMetrics(JdbcTemplate jdbc) {
            return new OutboxMetrics(jdbc);
        }
    }
}
