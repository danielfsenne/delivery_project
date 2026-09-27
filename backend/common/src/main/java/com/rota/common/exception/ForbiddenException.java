package com.rota.common.exception;

/**
 * Usuário autenticado sem permissão sobre o recurso (ex.: acessar pedido de outro cliente).
 * Mapeada para HTTP 403.
 */
public class ForbiddenException extends RuntimeException {

    public ForbiddenException(String message) {
        super(message);
    }
}
