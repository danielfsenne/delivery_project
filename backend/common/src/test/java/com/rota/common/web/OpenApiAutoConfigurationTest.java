package com.rota.common.web;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.servers.Server;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class OpenApiAutoConfigurationTest {

    private final WebApplicationContextRunner runner = new WebApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(OpenApiAutoConfiguration.class))
            .withPropertyValues("spring.application.name=order-service");

    @Test
    void specApontaParaOGatewayEExigeJwt() {
        runner.run(context -> {
            OpenAPI api = context.getBean(OpenAPI.class);

            assertThat(api.getInfo().getTitle()).isEqualTo("Rota - order-service");
            assertThat(api.getServers()).extracting(Server::getUrl).containsExactly("/api", "/");
            assertThat(api.getComponents().getSecuritySchemes()).containsKey(OpenApiAutoConfiguration.BEARER_SCHEME);
            assertThat(api.getSecurity()).hasSize(1);
        });
    }

    @Test
    void endpointsInternosFicamForaDaSpec() {
        runner.run(context -> {
            OpenAPI api = new OpenAPI().paths(new Paths()
                    .addPathItem("/orders", new PathItem())
                    .addPathItem("/internal/orders/{id}/driver", new PathItem()));

            context.getBean(OpenApiCustomizer.class).customise(api);

            assertThat(api.getPaths()).containsOnlyKeys("/orders");
        });
    }

    @Test
    void naoSeAplicaForaDeServicosServlet() {
        new ApplicationContextRunner()
                .withConfiguration(AutoConfigurations.of(OpenApiAutoConfiguration.class))
                .run(context -> assertThat(context).doesNotHaveBean(OpenAPI.class));
    }
}
