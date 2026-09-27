package com.rota.auth.interfaces.rest.dto;

import com.rota.common.security.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(max = 120) String name,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Size(min = 8, max = 72, message = "A senha deve ter entre 8 e 72 caracteres") String password,
        @NotNull Role role,
        @Pattern(regexp = "^\\+?[0-9 ()-]{8,20}$", message = "Telefone inválido") String phone
) {
}
