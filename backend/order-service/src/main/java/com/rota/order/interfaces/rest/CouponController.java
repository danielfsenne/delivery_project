package com.rota.order.interfaces.rest;

import com.rota.order.application.CouponService;
import com.rota.order.interfaces.rest.dto.CouponDtos.CouponRequest;
import com.rota.order.interfaces.rest.dto.CouponDtos.CouponResponse;
import com.rota.order.interfaces.rest.dto.CouponDtos.CouponStatusRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestão de cupons (administrador).
 */
@RestController
@RequestMapping("/orders/coupons")
@PreAuthorize("hasRole('ADMIN')")
public class CouponController {

    private final CouponService couponService;

    public CouponController(CouponService couponService) {
        this.couponService = couponService;
    }

    @GetMapping
    public List<CouponResponse> list() {
        return couponService.list().stream().map(CouponResponse::from).toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CouponResponse create(@Valid @RequestBody CouponRequest request) {
        return CouponResponse.from(couponService.create(request));
    }

    @PatchMapping("/{id}/status")
    public CouponResponse setActive(@PathVariable Long id, @Valid @RequestBody CouponStatusRequest request) {
        return CouponResponse.from(couponService.setActive(id, request.active()));
    }
}
