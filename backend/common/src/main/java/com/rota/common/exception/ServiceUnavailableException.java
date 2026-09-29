package com.rota.common.exception;

/**
 * Dependência externa indisponível (outro serviço fora do ar). Mapeada para HTTP 503.
 */
public class ServiceUnavailableException extends RuntimeException {

    public ServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
