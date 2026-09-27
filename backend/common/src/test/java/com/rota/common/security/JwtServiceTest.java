package com.rota.common.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private static final String SECRET = "test-secret-with-at-least-thirty-two-bytes!!";
    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final JwtProperties properties = new JwtProperties(SECRET, Duration.ofMinutes(15), null);
    private final AuthenticatedUser user = new AuthenticatedUser(42L, "ana@rota.dev", Role.CUSTOMER);

    @Test
    void shouldGenerateAndParseToken() {
        JwtService service = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC));

        String token = service.generateAccessToken(user);

        assertThat(service.parse(token)).contains(user);
    }

    @Test
    void shouldRejectExpiredToken() {
        String token = new JwtService(properties, Clock.fixed(NOW, ZoneOffset.UTC)).generateAccessToken(user);
        JwtService later = new JwtService(properties, Clock.fixed(NOW.plus(Duration.ofMinutes(16)), ZoneOffset.UTC));

        assertThat(later.parse(token)).isEmpty();
    }

    @Test
    void shouldRejectTokenSignedWithAnotherSecret() {
        JwtService other = new JwtService(
                new JwtProperties("another-secret-with-at-least-thirty-two-bytes", null, null));
        String token = other.generateAccessToken(user);

        assertThat(new JwtService(properties).parse(token)).isEmpty();
    }

    @Test
    void shouldRejectGarbage() {
        assertThat(new JwtService(properties).parse("not-a-jwt")).isEmpty();
    }
}
