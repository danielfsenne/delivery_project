package com.rota.auth.application;

import com.rota.auth.domain.User;
import com.rota.auth.domain.UserRepository;
import com.rota.auth.interfaces.rest.dto.AuthResponse;
import com.rota.auth.interfaces.rest.dto.LoginRequest;
import com.rota.auth.interfaces.rest.dto.RegisterRequest;
import com.rota.auth.interfaces.rest.dto.UserResponse;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.exception.UnauthorizedException;
import com.rota.common.security.JwtService;
import com.rota.common.security.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private static final String INVALID_CREDENTIALS = "E-mail ou senha inválidos";
    private static final String BLOCKED_ACCOUNT = "Conta bloqueada. Fale com o suporte.";

    private final UserRepository users;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokens;

    public AuthService(UserRepository users, PasswordEncoder passwordEncoder, JwtService jwtService,
                       RefreshTokenService refreshTokens) {
        this.users = users;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokens = refreshTokens;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (!request.role().isSelfRegistrable()) {
            throw new BusinessException("Não é permitido se cadastrar com o perfil " + request.role());
        }
        String email = User.normalizeEmail(request.email());
        if (users.existsByEmail(email)) {
            throw new ConflictException("E-mail já cadastrado");
        }
        User user = users.save(new User(request.name().trim(), email,
                passwordEncoder.encode(request.password()), request.role(), request.phone()));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = users.findByEmail(User.normalizeEmail(request.email()))
                .filter(u -> passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(() -> new UnauthorizedException(INVALID_CREDENTIALS));
        // Só depois da senha certa: quem erra a senha não descobre que a conta está bloqueada.
        if (!user.isActive()) {
            throw new ForbiddenException(BLOCKED_ACCOUNT);
        }
        return issueTokens(user);
    }

    @Transactional(noRollbackFor = UnauthorizedException.class)
    public AuthResponse refresh(String refreshToken) {
        User user = refreshTokens.consume(refreshToken);
        if (!user.isActive()) {
            throw new UnauthorizedException(BLOCKED_ACCOUNT);
        }
        return issueTokens(user);
    }

    public void logout(String refreshToken) {
        refreshTokens.revoke(refreshToken);
    }

    @Transactional(readOnly = true)
    public UserResponse me(Long userId) {
        return users.findById(userId)
                .map(UserResponse::from)
                .orElseThrow(() -> NotFoundException.of("Usuário", userId));
    }

    private AuthResponse issueTokens(User user) {
        String accessToken = jwtService.generateAccessToken(user.toAuthenticatedUser());
        String refreshToken = refreshTokens.issue(user);
        return new AuthResponse(accessToken, refreshToken, jwtService.accessTokenTtlSeconds(), UserResponse.from(user));
    }
}
