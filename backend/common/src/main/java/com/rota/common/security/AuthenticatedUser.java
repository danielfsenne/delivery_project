package com.rota.common.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Usuário extraído do access token. É o principal do {@link Authentication} em todos os serviços.
 */
public record AuthenticatedUser(Long id, String email, Role role) {

    public boolean hasRole(Role other) {
        return role == other;
    }

    public static AuthenticatedUser current() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof AuthenticatedUser user)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto");
        }
        return user;
    }
}
