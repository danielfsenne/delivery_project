package com.rota.common.messaging;

import com.rota.common.events.IntegrationEvent;

/**
 * Publica eventos de integração. A implementação padrão grava no outbox, na mesma transação
 * da mudança de estado: o evento só existe se a mudança for confirmada, e nunca se perde
 * se o RabbitMQ estiver fora no momento do commit.
 */
public interface EventPublisher {

    /**
     * @throws IllegalStateException se chamado fora de uma transação
     */
    void publish(String routingKey, IntegrationEvent event);
}
