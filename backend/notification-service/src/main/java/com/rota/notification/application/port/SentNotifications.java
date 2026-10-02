package com.rota.notification.application.port;

import java.util.UUID;

/**
 * Registro de notificações já enviadas, para não mandar o mesmo e-mail duas vezes
 * quando um evento é reentregue.
 */
public interface SentNotifications {

    /**
     * Reserva o envio. @return {@code false} se o evento já foi (ou está sendo) notificado
     */
    boolean reserve(UUID eventId);

    /** Desfaz a reserva quando o envio falha, para a nova tentativa poder enviar. */
    void release(UUID eventId);
}
