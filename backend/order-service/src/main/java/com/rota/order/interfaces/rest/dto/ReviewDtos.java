package com.rota.order.interfaces.rest.dto;

import com.rota.order.domain.review.Review;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public final class ReviewDtos {

    private ReviewDtos() {
    }

    public record ReviewRequest(
            @Min(1) @Max(5) int foodRating,
            @Min(1) @Max(5) Integer deliveryRating,
            @Size(max = 500) String comment
    ) {
    }

    /** Não expõe o id do cliente em listagens públicas. */
    public record ReviewResponse(Long orderId, Long restaurantId, int foodRating, Integer deliveryRating,
                                 String comment, Instant createdAt) {
        public static ReviewResponse from(Review r) {
            return new ReviewResponse(r.getOrderId(), r.getRestaurantId(), r.getFoodRating(), r.getDeliveryRating(),
                    r.getComment(), r.getCreatedAt());
        }
    }
}
