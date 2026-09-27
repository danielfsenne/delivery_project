package com.rota.order.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.CheckoutService;
import com.rota.order.application.OrderService;
import com.rota.order.domain.OrderStatus;
import com.rota.order.interfaces.rest.dto.OrderDtos.CancelRequest;
import com.rota.order.interfaces.rest.dto.OrderDtos.CheckoutRequest;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderSummaryResponse;
import com.rota.order.interfaces.rest.dto.OrderDtos.StatusChangeRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;

    public OrderController(CheckoutService checkoutService, OrderService orderService) {
        this.checkoutService = checkoutService;
        this.orderService = orderService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public OrderResponse checkout(@AuthenticationPrincipal AuthenticatedUser user,
                                  @Valid @RequestBody CheckoutRequest request) {
        return checkoutService.checkout(user.id(), request);
    }

    @GetMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Page<OrderSummaryResponse> mine(@AuthenticationPrincipal AuthenticatedUser user,
                                           @PageableDefault(size = 20) Pageable pageable) {
        return orderService.listForCustomer(user, pageable);
    }

    @GetMapping("/{id}")
    public OrderResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return orderService.get(user, id);
    }

    @PatchMapping("/{id}/status")
    public OrderResponse changeStatus(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                      @Valid @RequestBody StatusChangeRequest request) {
        return orderService.changeStatus(user, id, request.status(), request.reason());
    }

    @PostMapping("/{id}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                @Valid @RequestBody(required = false) CancelRequest request) {
        return orderService.cancel(user, id, request == null ? null : request.reason());
    }

    @GetMapping("/restaurant/{restaurantId}")
    public Page<OrderResponse> forRestaurant(@AuthenticationPrincipal AuthenticatedUser user,
                                             @PathVariable Long restaurantId,
                                             @RequestParam(required = false) List<OrderStatus> status,
                                             @PageableDefault(size = 50) Pageable pageable) {
        return orderService.listForRestaurant(user, restaurantId, status, pageable);
    }
}
