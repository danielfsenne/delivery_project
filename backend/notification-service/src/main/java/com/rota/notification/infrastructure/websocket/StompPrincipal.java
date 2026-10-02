package com.rota.notification.infrastructure.websocket;

import com.rota.common.security.AuthenticatedUser;

import java.security.Principal;

/**
 * Usuário da sessão STOMP. O nome é o id: é por ele que {@code convertAndSendToUser} encontra a sessão.
 */
public record StompPrincipal(AuthenticatedUser user) implements Principal {

    @Override
    public String getName() {
        return String.valueOf(user.id());
    }
}
