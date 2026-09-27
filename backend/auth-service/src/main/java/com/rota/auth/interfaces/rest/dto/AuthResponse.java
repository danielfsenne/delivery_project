package com.rota.auth.interfaces.rest.dto;

public record AuthResponse(String accessToken, String refreshToken, long expiresIn, UserResponse user) {
}
