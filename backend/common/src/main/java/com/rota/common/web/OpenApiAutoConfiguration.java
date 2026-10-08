package com.rota.common.web;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

/**
 * Documentação OpenAPI igual em todos os serviços. O gateway junta as specs num Swagger UI só
 * ({@code /swagger-ui.html} na porta 8080), então o primeiro servidor é o próprio gateway ({@code /api}):
 * o "Try it out" passa pelas mesmas rotas, filtros e rate limit que o front usa.
 */
@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@ConditionalOnClass(OpenAPI.class)
public class OpenApiAutoConfiguration {

    static final String BEARER_SCHEME = "bearer-jwt";

    @Bean
    @ConditionalOnMissingBean
    OpenAPI rotaOpenApi(@Value("${spring.application.name}") String serviceName) {
        return new OpenAPI()
                .info(new Info()
                        .title("Rota - " + serviceName)
                        .version("v1")
                        .description("Faça login em POST /auth/login (auth-service) e use o accessToken em Authorize."))
                .servers(List.of(
                        new Server().url("/api").description("Via API Gateway"),
                        new Server().url("/").description("Direto no serviço")))
                .components(new Components().addSecuritySchemes(BEARER_SCHEME, new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")))
                .addSecurityItem(new SecurityRequirement().addList(BEARER_SCHEME));
    }

    /** Endpoints /internal/** só existem entre serviços (o gateway os bloqueia): ficam fora da spec. */
    @Bean
    OpenApiCustomizer hideInternalEndpoints() {
        return openApi -> {
            if (openApi.getPaths() != null) {
                openApi.getPaths().keySet().removeIf(path -> path.startsWith("/internal"));
            }
        };
    }
}
