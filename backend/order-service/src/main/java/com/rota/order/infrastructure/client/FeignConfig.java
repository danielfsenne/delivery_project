package com.rota.order.infrastructure.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Clientes que agem em nome do usuário: repassam o token recebido na requisição.
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
        return new DomainErrorDecoder(objectMapper);
    }
}
