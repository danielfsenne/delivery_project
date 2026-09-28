package com.rota.auth.application;

import com.rota.auth.domain.User;
import com.rota.auth.domain.UserRepository;
import com.rota.auth.interfaces.rest.dto.AuthResponse;
import com.rota.auth.interfaces.rest.dto.LoginRequest;
import com.rota.auth.interfaces.rest.dto.RegisterRequest;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.UnauthorizedException;
import com.rota.common.security.JwtProperties;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthServiceTest {

    private final UserRepository users = mock(UserRepository.class);
    private final RefreshTokenService refreshTokens = mock(RefreshTokenService.class);
    private final PasswordEncoder encoder = new BCryptPasswordEncoder(4);
    private final JwtService jwtService =
            new JwtService(new JwtProperties("test-secret-with-at-least-thirty-two-bytes!!", null, null));

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(users, encoder, jwtService, refreshTokens);
        when(refreshTokens.issue(any())).thenReturn("refresh-token");
        when(users.save(any())).thenAnswer(inv -> withId(inv.getArgument(0), 1L));
    }

    @Test
    void shouldRegisterCustomerWithHashedPasswordAndNormalizedEmail() {
        AuthResponse response = service.register(
                new RegisterRequest("Ana", "  Ana@Rota.DEV ", "senha-forte", Role.CUSTOMER, null));

        assertThat(response.user().email()).isEqualTo("ana@rota.dev");
        assertThat(response.accessToken()).isNotBlank();
        assertThat(response.refreshToken()).isEqualTo("refresh-token");
        verify(users).save(org.mockito.ArgumentMatchers.argThat(u ->
                encoder.matches("senha-forte", u.getPasswordHash())));
    }

    @Test
    void shouldRejectDuplicatedEmail() {
        when(users.existsByEmail("ana@rota.dev")).thenReturn(true);

        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Ana", "ana@rota.dev", "senha-forte", Role.CUSTOMER, null)))
                .isInstanceOf(ConflictException.class);
        verify(users, never()).save(any());
    }

    @Test
    void shouldNotAllowSelfRegistrationAsAdmin() {
        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Eve", "eve@rota.dev", "senha-forte", Role.ADMIN, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldNotAllowSelfRegistrationAsService() {
        assertThatThrownBy(() -> service.register(
                new RegisterRequest("Bot", "bot@rota.dev", "senha-forte", Role.SERVICE, null)))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldLoginWithValidCredentials() {
        User user = withId(new User("Ana", "ana@rota.dev", encoder.encode("senha-forte"), Role.CUSTOMER, null), 7L);
        when(users.findByEmail("ana@rota.dev")).thenReturn(Optional.of(user));

        AuthResponse response = service.login(new LoginRequest("ANA@rota.dev", "senha-forte"));

        assertThat(jwtService.parse(response.accessToken()))
                .hasValueSatisfying(u -> assertThat(u.id()).isEqualTo(7L));
    }

    @Test
    void shouldRejectWrongPassword() {
        User user = withId(new User("Ana", "ana@rota.dev", encoder.encode("senha-forte"), Role.CUSTOMER, null), 7L);
        when(users.findByEmail("ana@rota.dev")).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> service.login(new LoginRequest("ana@rota.dev", "errada")))
                .isInstanceOf(UnauthorizedException.class);
    }

    private static User withId(User user, Long id) {
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }
}
