package com.rota.common.exception;

/**
 * Violação de regra de negócio. Mapeada para HTTP 422.
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
