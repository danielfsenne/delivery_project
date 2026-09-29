package com.rota.order.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;
import feign.Response;
import feign.codec.ErrorDecoder;

import java.io.IOException;
import java.io.InputStream;

/**
 * Converte respostas de erro dos outros serviços nas exceções de domínio equivalentes,
 * preservando a mensagem original.
 */
public class DomainErrorDecoder implements ErrorDecoder {

    private final ObjectMapper objectMapper;
    private final ErrorDecoder fallback = new ErrorDecoder.Default();

    public DomainErrorDecoder(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public Exception decode(String methodKey, Response response) {
        return switch (response.status()) {
            case 404 -> new NotFoundException(message(response, "Recurso não encontrado"));
            case 409 -> new ConflictException(message(response, "Conflito"));
            case 400, 422 -> new BusinessException(message(response, "Requisição inválida"));
            default -> fallback.decode(methodKey, response);
        };
    }

    private String message(Response response, String defaultMessage) {
        if (response.body() == null) {
            return defaultMessage;
        }
        try (InputStream body = response.body().asInputStream()) {
            JsonNode node = objectMapper.readTree(body);
            return node.hasNonNull("message") ? node.get("message").asText() : defaultMessage;
        } catch (IOException e) {
            return defaultMessage;
        }
    }
}
