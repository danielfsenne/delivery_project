package com.rota.gateway.security;

import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.JwtService;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Primeira barreira de segurança. Cada serviço continua validando o token e as permissões;
 * aqui só se barra cedo o que nunca deveria chegar até eles:
 * <ul>
 *   <li>token presente mas inválido ou expirado: 401, para o front renovar a sessão;</li>
 *   <li>endpoints {@code /internal/**}: só existem entre serviços, nunca passam pelo gateway.</li>
 * </ul>
 * O usuário válido fica disponível para os demais filtros (ex.: rate limit por usuário).
 */
@Component
public class JwtGatewayFilter implements GlobalFilter, Ordered {

    public static final String USER_ATTRIBUTE = "rota.user";
    public static final int ORDER = Ordered.HIGHEST_PRECEDENCE + 10;

    private static final String BEARER = "Bearer ";
    private static final Pattern INTERNAL_PATH = Pattern.compile("(^|/)(internal)(/|$)|(^|/)\\.\\.(/|$)");

    private final JwtService jwtService;

    public JwtGatewayFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        if (INTERNAL_PATH.matcher(request.getPath().value()).find()) {
            return reject(exchange, HttpStatus.NOT_FOUND, "Recurso não encontrado");
        }

        Optional<String> token = bearerToken(request);
        if (token.isEmpty()) {
            return chain.filter(exchange);
        }
        Optional<AuthenticatedUser> user = jwtService.parse(token.get());
        if (user.isEmpty()) {
            return reject(exchange, HttpStatus.UNAUTHORIZED, "Token inválido ou expirado");
        }
        exchange.getAttributes().put(USER_ATTRIBUTE, user.get());
        return chain.filter(exchange);
    }

    private static Optional<String> bearerToken(ServerHttpRequest request) {
        String header = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(BEARER)) {
            return Optional.empty();
        }
        return Optional.of(header.substring(BEARER.length()));
    }

    /** Mesmo formato de erro dos serviços ({@code ApiError}), para o front tratar tudo igual. */
    private static Mono<Void> reject(ServerWebExchange exchange, HttpStatus status, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"timestamp":"%s","status":%d,"error":"%s","message":"%s","path":"%s"}"""
                .formatted(Instant.now(), status.value(), status.getReasonPhrase(), message,
                        exchange.getRequest().getPath().value().replace("\"", ""));
        return response.writeWith(Mono.just(response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8))));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
