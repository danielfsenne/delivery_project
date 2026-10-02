package com.rota.notification.infrastructure.websocket;

import com.rota.notification.application.RealtimeMessage;
import com.rota.notification.application.port.RealtimeGateway;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class StompRealtimeGateway implements RealtimeGateway {

    /** No cliente: {@code /user/queue/events}. */
    static final String USER_QUEUE = "/queue/events";

    private final SimpMessagingTemplate messaging;

    public StompRealtimeGateway(SimpMessagingTemplate messaging) {
        this.messaging = messaging;
    }

    @Override
    public void toUser(Long userId, RealtimeMessage message) {
        if (userId != null) {
            messaging.convertAndSendToUser(String.valueOf(userId), USER_QUEUE, message);
        }
    }

    @Override
    public void toDrivers(RealtimeMessage message) {
        messaging.convertAndSend(StompAuthInterceptor.DRIVERS_TOPIC, message);
    }
}
