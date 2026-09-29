package com.rota.common.security;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

/**
 * Fornece o token de serviço usado nos clientes HTTP internos, renovando-o
 * um pouco antes de expirar.
 */
public class ServiceTokenProvider {

    private static final Duration RENEW_BEFORE = Duration.ofMinutes(1);

    private final JwtService jwtService;
    private final String serviceName;
    private final Clock clock;

    private String token;
    private Instant expiresAt;

    public ServiceTokenProvider(JwtService jwtService, String serviceName, Clock clock) {
        this.jwtService = jwtService;
        this.serviceName = serviceName;
        this.clock = clock;
    }

    public synchronized String token() {
        Instant now = clock.instant();
        if (token == null || !now.isBefore(expiresAt.minus(RENEW_BEFORE))) {
            token = jwtService.generateServiceToken(serviceName);
            expiresAt = now.plusSeconds(jwtService.accessTokenTtlSeconds());
        }
        return token;
    }

    public String bearer() {
        return "Bearer " + token();
    }
}
