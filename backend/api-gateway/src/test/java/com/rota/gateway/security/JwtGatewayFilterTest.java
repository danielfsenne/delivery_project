package com.rota.gateway.security;

import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.JwtProperties;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class JwtGatewayFilterTest {

    private final JwtService jwtService = new JwtService(
            new JwtProperties("test-secret-with-at-least-32-bytes-ok", Duration.ofMinutes(15), Duration.ofDays(7)));
    private final JwtGatewayFilter filter = new JwtGatewayFilter(jwtService);

    @Test
    void requisicaoSemTokenSegueAdiante() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/restaurants"));
        var chain = new RecordingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.called).isTrue();
    }

    @Test
    void tokenValidoSegueComOUsuarioNoExchange() {
        String token = jwtService.generateAccessToken(new AuthenticatedUser(7L, "a@rota.dev", Role.CUSTOMER));
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token));
        var chain = new RecordingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.called).isTrue();
        AuthenticatedUser user = exchange.getAttribute(JwtGatewayFilter.USER_ATTRIBUTE);
        assertThat(user).isNotNull();
        assertThat(user.id()).isEqualTo(7L);
    }

    @Test
    void tokenInvalidoRecebe401() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer token-falso"));
        var chain = new RecordingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.called).isFalse();
        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void endpointsInternosNaoPassamPeloGateway() {
        for (String path : new String[]{"/api/internal/payments", "/api/orders/../internal/orders/1/status"}) {
            var exchange = MockServerWebExchange.from(MockServerHttpRequest.post(path));
            var chain = new RecordingChain();

            filter.filter(exchange, chain).block();

            assertThat(chain.called).as(path).isFalse();
            assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        }
    }

    @Test
    void palavraInternalDentroDeOutroSegmentoNaoEhBloqueada() {
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/api/restaurants/internalize"));
        var chain = new RecordingChain();

        filter.filter(exchange, chain).block();

        assertThat(chain.called).isTrue();
    }

    private static class RecordingChain implements GatewayFilterChain {
        private boolean called;

        @Override
        public Mono<Void> filter(org.springframework.web.server.ServerWebExchange exchange) {
            called = true;
            return Mono.empty();
        }
    }
}
