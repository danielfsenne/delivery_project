package com.rota.common.exception;

/**
 * Credenciais ou token inválidos. Mapeada para HTTP 401.
 */
public class UnauthorizedException extends RuntimeException {

    public UnauthorizedException(String message) {
        super(message);
    }
}
