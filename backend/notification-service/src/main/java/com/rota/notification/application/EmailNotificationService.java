package com.rota.notification.application;

import com.rota.common.events.OrderStatusChanged;
import com.rota.notification.application.port.EmailSender;
import com.rota.notification.application.port.SentNotifications;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * E-mails de pedido. Se o envio falhar, a exceção sobe: o listener tenta de novo e,
 * esgotadas as tentativas, a mensagem vai para a DLQ.
 */
public class EmailNotificationService {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationService.class);

    private final OrderEmailComposer composer;
    private final EmailSender sender;
    private final SentNotifications sent;

    public EmailNotificationService(OrderEmailComposer composer, EmailSender sender, SentNotifications sent) {
        this.composer = composer;
        this.sender = sender;
        this.sent = sent;
    }

    public void onOrderStatusChanged(OrderStatusChanged event) {
        composer.compose(event).ifPresent(email -> {
            if (!sent.reserve(event.eventId())) {
                log.info("E-mail do evento {} já enviado; ignorando reentrega", event.eventId());
                return;
            }
            try {
                sender.send(email);
                log.info("E-mail '{}' enviado para o pedido {}", email.subject(), event.orderId());
            } catch (RuntimeException e) {
                sent.release(event.eventId());
                throw e;
            }
        });
    }
}
