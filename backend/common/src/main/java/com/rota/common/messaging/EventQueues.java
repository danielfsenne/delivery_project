package com.rota.common.messaging;

import com.rota.common.events.RotaEvents;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Declarable;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.QueueBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * Declara a fila de um consumidor já com a sua DLQ.
 *
 * <pre>
 * &#64;Bean
 * Declarables orderReadyQueue() {
 *     return EventQueues.durable("delivery.order-ready", RotaEvents.ORDER_READY_FOR_PICKUP);
 * }
 * </pre>
 *
 * Quando o listener falha em todas as tentativas, a mensagem é rejeitada e o RabbitMQ
 * a move para {@code <fila>.dlq}, onde fica para análise em vez de ser perdida.
 */
public final class EventQueues {

    public static final String DLQ_SUFFIX = ".dlq";

    private EventQueues() {
    }

    public static Declarables durable(String queueName, String... routingKeys) {
        Queue queue = QueueBuilder.durable(queueName)
                .deadLetterExchange(RotaEvents.DEAD_LETTER_EXCHANGE)
                .deadLetterRoutingKey(queueName)
                .build();
        Queue deadLetterQueue = QueueBuilder.durable(queueName + DLQ_SUFFIX).build();

        List<Declarable> declarables = new ArrayList<>();
        declarables.add(queue);
        declarables.add(deadLetterQueue);
        declarables.add(new Binding(deadLetterQueue.getName(), Binding.DestinationType.QUEUE,
                RotaEvents.DEAD_LETTER_EXCHANGE, queueName, null));
        for (String routingKey : routingKeys) {
            declarables.add(new Binding(queueName, Binding.DestinationType.QUEUE, RotaEvents.EXCHANGE,
                    routingKey, null));
        }
        return new Declarables(declarables);
    }

    /**
     * Fila exclusiva e temporária (some quando a instância cai). Para eventos efêmeros que
     * cada instância precisa receber, como posições do entregador repassadas via WebSocket.
     */
    public static Declarables transientPerInstance(String queueName, String... routingKeys) {
        Queue queue = QueueBuilder.nonDurable(queueName).exclusive().autoDelete().build();
        List<Declarable> declarables = new ArrayList<>();
        declarables.add(queue);
        for (String routingKey : routingKeys) {
            declarables.add(new Binding(queueName, Binding.DestinationType.QUEUE, RotaEvents.EXCHANGE,
                    routingKey, null));
        }
        return new Declarables(declarables);
    }
}
