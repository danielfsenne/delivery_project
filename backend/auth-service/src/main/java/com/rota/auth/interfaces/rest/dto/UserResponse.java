package com.rota.auth.interfaces.rest.dto;

import com.rota.auth.domain.User;
import com.rota.common.security.Role;

public record UserResponse(Long id, String name, String email, Role role, String phone) {

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getRole(), user.getPhone());
    }
}
