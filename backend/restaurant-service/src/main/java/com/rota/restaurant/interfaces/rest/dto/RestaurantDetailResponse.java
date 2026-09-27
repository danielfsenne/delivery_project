package com.rota.restaurant.interfaces.rest.dto;

import com.rota.restaurant.domain.Address;
import com.rota.restaurant.domain.Category;
import com.rota.restaurant.domain.OpeningHour;
import com.rota.restaurant.domain.Product;
import com.rota.restaurant.domain.ProductOption;
import com.rota.restaurant.domain.Restaurant;

import java.math.BigDecimal;
import java.util.List;

public record RestaurantDetailResponse(
        Long id,
        Long ownerId,
        String name,
        String description,
        String cuisine,
        String phone,
        String imageUrl,
        BigDecimal deliveryFee,
        BigDecimal minOrderValue,
        int deliveryTimeMin,
        int deliveryTimeMax,
        boolean active,
        boolean open,
        Address address,
        List<OpeningHour> openingHours,
        List<CategoryResponse> categories
) {
    public static RestaurantDetailResponse from(Restaurant r, boolean open) {
        return new RestaurantDetailResponse(r.getId(), r.getOwnerId(), r.getName(), r.getDescription(),
                r.getCuisine(), r.getPhone(), r.getImageUrl(), r.getDeliveryFee(), r.getMinOrderValue(),
                r.getDeliveryTimeMin(), r.getDeliveryTimeMax(), r.isActive(), open, r.getAddress(),
                r.getOpeningHours(), r.getCategories().stream().map(CategoryResponse::from).toList());
    }

    public record CategoryResponse(Long id, String name, int position, List<ProductResponse> products) {
        public static CategoryResponse from(Category c) {
            return new CategoryResponse(c.getId(), c.getName(), c.getPosition(),
                    c.getProducts().stream().map(ProductResponse::from).toList());
        }
    }

    public record ProductResponse(Long id, String name, String description, BigDecimal price, String imageUrl,
                                  boolean available, List<OptionResponse> options) {
        public static ProductResponse from(Product p) {
            return new ProductResponse(p.getId(), p.getName(), p.getDescription(), p.getPrice(), p.getImageUrl(),
                    p.isAvailable(), p.getOptions().stream().map(OptionResponse::from).toList());
        }
    }

    public record OptionResponse(Long id, String name, BigDecimal price) {
        public static OptionResponse from(ProductOption o) {
            return new OptionResponse(o.getId(), o.getName(), o.getPrice());
        }
    }
}
