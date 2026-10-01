package com.rota.delivery.infrastructure.messaging;

import com.rota.common.events.DriverLocationUpdated;
import com.rota.common.events.RotaEvents;
import com.rota.delivery.application.port.LocationBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Clock;
import java.util.UUID;

/**
 * Posição é efêmera: vai direto ao RabbitMQ, sem outbox. Se houver transação, o envio
 * espera o commit, para não anunciar uma posição que acabou não sendo gravada.
 */
@Component
public class RabbitLocationBroadcaster implements LocationBroadcaster {

    private static final Logger log = LoggerFactory.getLogger(RabbitLocationBroadcaster.class);

    private final RabbitTemplate rabbit;
    private final Clock clock;

    public RabbitLocationBroadcaster(RabbitTemplate rabbit, Clock clock) {
        this.rabbit = rabbit;
        this.clock = clock;
    }

    @Override
    public void driverMoved(Long driverId, Long orderId, Long customerId, double latitude, double longitude) {
        DriverLocationUpdated event = new DriverLocationUpdated(UUID.randomUUID(), driverId, orderId, customerId,
                latitude, longitude, clock.instant());
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(event);
                }
            });
        } else {
            send(event);
        }
    }

    private void send(DriverLocationUpdated event) {
        try {
            rabbit.convertAndSend(RotaEvents.EXCHANGE, RotaEvents.DRIVER_LOCATION_UPDATED, event);
        } catch (AmqpException e) {
            log.debug("Posição do entregador {} não publicada: {}", event.driverId(), e.getMessage());
        }
    }
}
