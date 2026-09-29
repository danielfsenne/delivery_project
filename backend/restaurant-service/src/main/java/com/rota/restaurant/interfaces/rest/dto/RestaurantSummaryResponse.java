package com.rota.restaurant.interfaces.rest.dto;

import com.rota.restaurant.domain.Restaurant;

import java.math.BigDecimal;

public record RestaurantSummaryResponse(
        Long id,
        String name,
        String description,
        String cuisine,
        String imageUrl,
        BigDecimal deliveryFee,
        BigDecimal minOrderValue,
        int deliveryTimeMin,
        int deliveryTimeMax,
        String city,
        String district,
        boolean active,
        boolean open,
        Double ratingAverage,
        int ratingCount
) {
    public static RestaurantSummaryResponse from(Restaurant r, boolean open) {
        return new RestaurantSummaryResponse(r.getId(), r.getName(), r.getDescription(), r.getCuisine(),
                r.getImageUrl(), r.getDeliveryFee(), r.getMinOrderValue(), r.getDeliveryTimeMin(),
                r.getDeliveryTimeMax(), r.getAddress().city(), r.getAddress().district(), r.isActive(), open,
                r.getRatingAverage(), r.getRatingCount());
    }
}
