package com.rota.order.interfaces.rest;

import com.rota.order.application.OrderService;
import com.rota.order.application.PlatformStatsService;
import com.rota.order.application.PlatformStatsService.PlatformStats;
import com.rota.order.domain.OrderStatus;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Visão da plataforma inteira (administrador). Detalhe, mudança de status e cancelamento
 * usam os endpoints normais de {@code /orders/{id}}, que já liberam o admin.
 */
@RestController
@RequestMapping("/orders/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminOrderController {

    private final OrderService orderService;
    private final PlatformStatsService statsService;

    public AdminOrderController(OrderService orderService, PlatformStatsService statsService) {
        this.orderService = orderService;
        this.statsService = statsService;
    }

    @GetMapping
    public Page<OrderSummaryResponse> list(@RequestParam(required = false) List<OrderStatus> status,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return orderService.listAll(status, pageable);
    }

    @GetMapping("/stats")
    public PlatformStats stats() {
        return statsService.today();
    }
}
