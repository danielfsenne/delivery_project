package com.rota.order.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.OrderService;
import com.rota.order.domain.OrderStatus;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints usados pelo delivery-service (perfil SERVICE).
 */
@RestController
@RequestMapping("/internal/orders")
public class InternalOrderController {

    private final OrderService orderService;

    public InternalOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/{id}/driver")
    public OrderResponse assignDriver(@PathVariable Long id, @Valid @RequestBody DriverRequest request) {
        return orderService.assignDriver(id, request.driverId());
    }

    /**
     * Mudança de status feita pelo entregador. O histórico registra o entregador como autor.
     */
    @PostMapping("/{id}/status")
    public OrderResponse changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        AuthenticatedUser driverActing = AuthenticatedUser.service("delivery-service");
        return orderService.changeStatusOnBehalf(driverActing, id, request.status(), request.actorId());
    }

    public record DriverRequest(@NotNull Long driverId) {
    }

    public record StatusRequest(@NotNull OrderStatus status, Long actorId) {
    }
}
