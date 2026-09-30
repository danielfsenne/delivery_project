package com.rota.common.feign;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.common.security.ServiceTokenProvider;
import feign.RequestInterceptor;
import feign.codec.ErrorDecoder;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;

/**
 * Clientes de endpoints {@code /internal/**}: autenticam com o token do próprio serviço.
 */
public class InternalFeignConfig {

    @Bean
    RequestInterceptor serviceAuthorization(ServiceTokenProvider tokens) {
        return template -> template.header(HttpHeaders.AUTHORIZATION, tokens.bearer());
    }

    @Bean
    ErrorDecoder errorDecoder(ObjectMapper objectMapper) {
        return new DomainErrorDecoder(objectMapper);
    }
}
