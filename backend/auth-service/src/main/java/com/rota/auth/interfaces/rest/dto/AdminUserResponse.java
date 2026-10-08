package com.rota.auth.interfaces.rest.dto;

import com.rota.auth.domain.User;
import com.rota.common.security.Role;

import java.time.Instant;

public record AdminUserResponse(Long id, String name, String email, Role role, String phone, boolean active,
                                Instant createdAt) {

    public static AdminUserResponse from(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone(),
                user.isActive(), user.getCreatedAt());
    }
}
