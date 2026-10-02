package com.rota.notification.application.port;

import com.rota.notification.application.RealtimeMessage;

/**
 * Entrega mensagens aos usuários conectados por WebSocket nesta instância.
 */
public interface RealtimeGateway {

    void toUser(Long userId, RealtimeMessage message);

    void toDrivers(RealtimeMessage message);
}
