package com.rota.notification.infrastructure.messaging;

import com.rota.common.events.OrderStatusChanged;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventQueues;
import com.rota.notification.application.EmailNotificationService;
import com.rota.notification.application.OrderEmailComposer;
import com.rota.notification.application.port.EmailSender;
import com.rota.notification.application.port.SentNotifications;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Fila durável e compartilhada: com várias instâncias, cada e-mail é enviado por uma só.
 */
@Configuration
public class EmailEventsListener {

    static final String EMAIL_QUEUE = "notification.order-email";

    private final EmailNotificationService emails;

    public EmailEventsListener(EmailSender sender, SentNotifications sent) {
        this.emails = new EmailNotificationService(new OrderEmailComposer(), sender, sent);
    }

    @Bean
    Declarables orderEmailQueue() {
        return EventQueues.durable(EMAIL_QUEUE, RotaEvents.ORDER_STATUS_CHANGED);
    }

    @RabbitListener(queues = EMAIL_QUEUE)
    public void onOrderStatusChanged(OrderStatusChanged event) {
        emails.onOrderStatusChanged(event);
    }
}
