package com.rota.order.infrastructure.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import feign.RequestInterceptor;
import feign.Response;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.io.IOException;
import java.io.InputStream;

/**
 * Configuração dos clientes Feign: repassa o token do usuário e converte
 * erros HTTP dos outros serviços em exceções de domínio.
 */
public class FeignConfig {

    @Bean
    RequestInterceptor forwardAuthorization() {
        return template -> {
            if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attrs) {
                String header = attrs.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
                if (header != null) {
                    template.header(HttpHeaders.AUTHORIZATION, header);
                }
            }
        };
    }

    @Bean
    ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        ErrorDecoder fallback = new ErrorDecoder.Default();
        return (methodKey, response) -> switch (response.status()) {
            case 404 -> new NotFoundException(message(objectMapper, response, "Recurso não encontrado"));
            case 400, 422 -> new BusinessException(message(objectMapper, response, "Requisição inválida"));
            default -> fallback.decode(methodKey, response);
        };
    }

    private static String message(ObjectMapper mapper, Response response, String defaultMessage) {
        if (response.body() == null) {
            return defaultMessage;
        }
        try (InputStream body = response.body().asInputStream()) {
            JsonNode node = mapper.readTree(body);
            return node.hasNonNull("message") ? node.get("message").asText() : defaultMessage;
        } catch (IOException e) {
            return defaultMessage;
        }
    }
}
