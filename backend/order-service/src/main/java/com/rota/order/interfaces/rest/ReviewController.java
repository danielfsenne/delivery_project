package com.rota.order.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.ReviewService;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewRequest;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @PostMapping("/orders/{id}/review")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('CUSTOMER')")
    public ReviewResponse create(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                 @Valid @RequestBody ReviewRequest request) {
        return reviewService.create(user, id, request);
    }

    @GetMapping("/orders/{id}/review")
    public ReviewResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return reviewService.forOrder(user, id);
    }

    /** Avaliações públicas de um restaurante. */
    @GetMapping("/orders/reviews")
    public Page<ReviewResponse> forRestaurant(@RequestParam Long restaurantId,
                                              @PageableDefault(size = 20) Pageable pageable) {
        return reviewService.forRestaurant(restaurantId, pageable);
    }
}
