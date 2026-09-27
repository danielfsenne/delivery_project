package com.rota.auth.application;

import com.rota.auth.domain.RefreshToken;
import com.rota.auth.domain.RefreshTokenRepository;
import com.rota.auth.domain.User;
import com.rota.common.exception.UnauthorizedException;
import com.rota.common.security.JwtProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.util.Base64;
import java.util.HexFormat;

/**
 * Refresh tokens com rotação: cada uso invalida o token e emite outro.
 * Se um token já revogado for reutilizado, assume-se vazamento e todos os tokens
 * do usuário são revogados.
 */
@Service
public class RefreshTokenService {

    private static final Logger log = LoggerFactory.getLogger(RefreshTokenService.class);
    private static final int TOKEN_BYTES = 32;

    private final RefreshTokenRepository repository;
    private final JwtProperties jwtProperties;
    private final Clock clock;
    private final SecureRandom random = new SecureRandom();

    public RefreshTokenService(RefreshTokenRepository repository, JwtProperties jwtProperties, Clock clock) {
        this.repository = repository;
        this.jwtProperties = jwtProperties;
        this.clock = clock;
    }

    @Transactional
    public String issue(User user) {
        byte[] bytes = new byte[TOKEN_BYTES];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        repository.save(new RefreshToken(user, hash(token), clock.instant().plus(jwtProperties.refreshTokenTtl())));
        return token;
    }

    /**
     * Valida o token, revoga-o e devolve o dono. O chamador deve emitir um novo par de tokens.
     */
    @Transactional(noRollbackFor = UnauthorizedException.class)
    public User consume(String token) {
        RefreshToken stored = repository.findByTokenHash(hash(token))
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        if (stored.isRevoked()) {
            Long userId = stored.getUser().getId();
            log.warn("Reuso de refresh token revogado detectado para o usuário {}", userId);
            repository.revokeAllByUserId(userId);
            throw new UnauthorizedException("Refresh token inválido");
        }
        if (stored.isExpired(clock.instant())) {
            throw new UnauthorizedException("Refresh token expirado");
        }
        stored.revoke();
        return stored.getUser();
    }

    @Transactional
    public void revoke(String token) {
        repository.findByTokenHash(hash(token)).ifPresent(RefreshToken::revoke);
    }

    static String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
