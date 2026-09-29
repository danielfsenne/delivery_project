package com.rota.common.security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class ServiceTokenProviderTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final AtomicReference<Instant> now = new AtomicReference<>(NOW);
    private final Clock clock = new Clock() {
        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now.get();
        }
    };
    private final JwtService jwt = new JwtService(
            new JwtProperties("test-secret-with-at-least-thirty-two-bytes!!", Duration.ofMinutes(15), null), clock);
    private final ServiceTokenProvider provider = new ServiceTokenProvider(jwt, "order-service", clock);

    @Test
    void shouldIssueValidServiceToken() {
        assertThat(jwt.parse(provider.token()))
                .hasValueSatisfying(u -> assertThat(u.role()).isEqualTo(Role.SERVICE));
        assertThat(provider.bearer()).startsWith("Bearer ");
    }

    @Test
    void shouldReuseTokenWhileValid() {
        String first = provider.token();
        now.set(NOW.plus(Duration.ofMinutes(10)));

        assertThat(provider.token()).isEqualTo(first);
    }

    @Test
    void shouldRenewTokenBeforeExpiring() {
        String first = provider.token();
        now.set(NOW.plus(Duration.ofMinutes(14)));

        String renewed = provider.token();

        assertThat(renewed).isNotEqualTo(first);
        assertThat(jwt.parse(renewed)).isPresent();
    }
}
