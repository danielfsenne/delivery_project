package com.rota.auth.application;

import com.rota.auth.domain.RefreshToken;
import com.rota.auth.domain.RefreshTokenRepository;
import com.rota.auth.domain.User;
import com.rota.common.exception.UnauthorizedException;
import com.rota.common.security.JwtProperties;
import com.rota.common.security.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RefreshTokenServiceTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private final RefreshTokenRepository repository = mock(RefreshTokenRepository.class);
    private final JwtProperties props =
            new JwtProperties("test-secret-with-at-least-thirty-two-bytes!!", null, Duration.ofDays(7));
    private final RefreshTokenService service =
            new RefreshTokenService(repository, props, Clock.fixed(NOW, ZoneOffset.UTC));
    private final User user = user();

    @Test
    void shouldRotateValidToken() {
        RefreshToken stored = new RefreshToken(user, RefreshTokenService.hash("abc"), NOW.plusSeconds(60));
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(stored));

        assertThat(service.consume("abc")).isSameAs(user);
        assertThat(stored.isRevoked()).isTrue();
    }

    @Test
    void shouldRevokeAllTokensWhenRevokedTokenIsReused() {
        RefreshToken stored = new RefreshToken(user, RefreshTokenService.hash("abc"), NOW.plusSeconds(60));
        stored.revoke();
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.consume("abc")).isInstanceOf(UnauthorizedException.class);
        verify(repository).revokeAllByUserId(user.getId());
    }

    @Test
    void shouldRejectExpiredToken() {
        RefreshToken stored = new RefreshToken(user, RefreshTokenService.hash("abc"), NOW);
        when(repository.findByTokenHash(RefreshTokenService.hash("abc"))).thenReturn(Optional.of(stored));

        assertThatThrownBy(() -> service.consume("abc"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("expirado");
    }

    @Test
    void shouldRejectUnknownToken() {
        assertThatThrownBy(() -> service.consume("nope")).isInstanceOf(UnauthorizedException.class);
    }

    private static User user() {
        User user = new User("Ana", "ana@rota.dev", "hash", Role.CUSTOMER, null);
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }
}
