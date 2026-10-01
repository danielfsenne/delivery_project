package com.rota.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.events.RotaEvents;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;

/**
 * Ativa-se em todo serviço que tem spring-amqp: declara os exchanges compartilhados
 * e usa JSON (com o ObjectMapper do Spring) nas mensagens.
 */
@AutoConfiguration(before = RabbitAutoConfiguration.class, after = JacksonAutoConfiguration.class)
@ConditionalOnClass(RabbitTemplate.class)
public class RabbitMessagingAutoConfiguration {

    @Bean
    TopicExchange rotaEventsExchange() {
        return new TopicExchange(RotaEvents.EXCHANGE, true, false);
    }

    @Bean
    DirectExchange rotaDeadLetterExchange() {
        return new DirectExchange(RotaEvents.DEAD_LETTER_EXCHANGE, true, false);
    }

    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }
}
