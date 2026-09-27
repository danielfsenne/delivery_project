package com.rota.common.exception;

/**
 * Recurso inexistente. Mapeada para HTTP 404.
 */
public class NotFoundException extends RuntimeException {

    public NotFoundException(String message) {
        super(message);
    }

    public static NotFoundException of(String resource, Object id) {
        return new NotFoundException(resource + " não encontrado: " + id);
    }
}
