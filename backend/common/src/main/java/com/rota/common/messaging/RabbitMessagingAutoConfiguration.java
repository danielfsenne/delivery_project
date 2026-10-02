package com.rota.common.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.events.IntegrationEvent;
import com.rota.common.events.RotaEvents;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.DefaultJackson2JavaTypeMapper;
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

    /**
     * Listeners com parâmetro tipado usam o tipo do método. Listeners que recebem vários eventos
     * na mesma fila ({@code @RabbitHandler}) usam o header {@code __TypeId__}, aceito apenas
     * para classes do pacote de eventos.
     */
    @Bean
    @ConditionalOnMissingBean(MessageConverter.class)
    MessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        Jackson2JsonMessageConverter converter = new Jackson2JsonMessageConverter(objectMapper);
        DefaultJackson2JavaTypeMapper typeMapper = new DefaultJackson2JavaTypeMapper();
        typeMapper.setTrustedPackages(IntegrationEvent.class.getPackageName());
        converter.setJavaTypeMapper(typeMapper);
        return converter;
    }
}
