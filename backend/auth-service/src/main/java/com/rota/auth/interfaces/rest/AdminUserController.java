package com.rota.auth.interfaces.rest;

import com.rota.auth.application.UserAdminService;
import com.rota.auth.interfaces.rest.dto.AdminUserResponse;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Gestão de usuários (administrador).
 */
@RestController
@RequestMapping("/auth/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final UserAdminService service;

    public AdminUserController(UserAdminService service) {
        this.service = service;
    }

    @GetMapping
    public Page<AdminUserResponse> search(@RequestParam(required = false) Role role,
                                          @RequestParam(required = false) String q,
                                          @PageableDefault(size = 20, sort = "createdAt",
                                                  direction = Sort.Direction.DESC) Pageable pageable) {
        return service.search(role, q, pageable);
    }

    @PatchMapping("/{id}/status")
    public AdminUserResponse setActive(@AuthenticationPrincipal AuthenticatedUser admin, @PathVariable Long id,
                                       @Valid @RequestBody StatusRequest request) {
        return service.setActive(admin, id, request.active());
    }

    public record StatusRequest(@NotNull Boolean active) {
    }
}
