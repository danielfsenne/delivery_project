package com.rota.auth.application;

import com.rota.auth.domain.RefreshTokenRepository;
import com.rota.auth.domain.User;
import com.rota.auth.domain.UserRepository;
import com.rota.auth.interfaces.rest.dto.AdminUserResponse;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gestão de contas pelo administrador.
 */
@Service
public class UserAdminService {

    private static final Logger log = LoggerFactory.getLogger(UserAdminService.class);

    private final UserRepository users;
    private final RefreshTokenRepository refreshTokens;

    public UserAdminService(UserRepository users, RefreshTokenRepository refreshTokens) {
        this.users = users;
        this.refreshTokens = refreshTokens;
    }

    @Transactional(readOnly = true)
    public Page<AdminUserResponse> search(Role role, String search, Pageable pageable) {
        Specification<User> filter = Specification.where(UserRepository.hasRole(role))
                .and(UserRepository.matches(search));
        return users.findAll(filter, pageable).map(AdminUserResponse::from);
    }

    /**
     * Bloquear derruba todas as sessões: os refresh tokens são revogados, e o access token
     * em uso expira sozinho em poucos minutos.
     */
    @Transactional
    public AdminUserResponse setActive(AuthenticatedUser admin, Long userId, boolean active) {
        if (admin.id().equals(userId)) {
            throw new BusinessException("Você não pode bloquear a própria conta");
        }
        User user = users.findById(userId).orElseThrow(() -> NotFoundException.of("Usuário", userId));
        user.setActive(active);
        if (!active) {
            refreshTokens.revokeAllByUserId(userId);
        }
        log.info("Admin {} {} o usuário {}", admin.id(), active ? "desbloqueou" : "bloqueou", userId);
        return AdminUserResponse.from(user);
    }
}
