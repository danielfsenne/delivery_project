package com.rota.common.exception;

/**
 * Estado atual do recurso impede a operação (ex.: transição de status inválida,
 * e-mail já cadastrado). Mapeada para HTTP 409.
 */
public class ConflictException extends RuntimeException {

    public ConflictException(String message) {
        super(message);
    }
}
