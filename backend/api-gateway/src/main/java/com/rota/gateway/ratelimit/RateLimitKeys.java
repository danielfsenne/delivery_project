package com.rota.gateway.ratelimit;

import com.rota.common.security.AuthenticatedUser;
import com.rota.gateway.security.JwtGatewayFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;

/**
 * Chaves do rate limit (token bucket no Redis).
 * O IP vem da conexão, não do X-Forwarded-For: o header pode ser forjado pelo cliente
 * e serviria para burlar o limite. Atrás de um proxy confiável, isso mudaria.
 */
@Configuration
public class RateLimitKeys {

    /** Limite geral: por usuário autenticado; sem token, por IP. */
    @Bean
    @Primary
    KeyResolver userOrIpKeyResolver() {
        return exchange -> {
            AuthenticatedUser user = exchange.getAttribute(JwtGatewayFilter.USER_ATTRIBUTE);
            return Mono.just(user != null ? "user:" + user.id() : "ip:" + clientIp(exchange));
        };
    }

    /** Login e cadastro: sempre por IP, para conter tentativas de senha em série. */
    @Bean
    KeyResolver loginKeyResolver() {
        return exchange -> Mono.just("login:" + clientIp(exchange));
    }

    static String clientIp(ServerWebExchange exchange) {
        InetSocketAddress remote = exchange.getRequest().getRemoteAddress();
        return remote == null || remote.getAddress() == null ? "unknown" : remote.getAddress().getHostAddress();
    }
}
