package com.rota.common.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * @param secret          chave HMAC compartilhada entre os serviços (mínimo 32 bytes)
 * @param accessTokenTtl  validade do access token
 * @param refreshTokenTtl validade do refresh token (usada apenas pelo auth-service)
 */
@ConfigurationProperties(prefix = "rota.jwt")
public record JwtProperties(String secret, Duration accessTokenTtl, Duration refreshTokenTtl) {

    public JwtProperties {
        if (secret == null || secret.getBytes().length < 32) {
            throw new IllegalArgumentException("rota.jwt.secret deve ter pelo menos 32 bytes");
        }
        if (accessTokenTtl == null) {
            accessTokenTtl = Duration.ofMinutes(15);
        }
        if (refreshTokenTtl == null) {
            refreshTokenTtl = Duration.ofDays(7);
        }
    }
}
