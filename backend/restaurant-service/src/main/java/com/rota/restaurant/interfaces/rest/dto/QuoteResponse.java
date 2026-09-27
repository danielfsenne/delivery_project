package com.rota.restaurant.interfaces.rest.dto;

import java.math.BigDecimal;
import java.util.List;

public record QuoteResponse(
        Long restaurantId,
        String restaurantName,
        Long ownerId,
        boolean open,
        BigDecimal deliveryFee,
        BigDecimal minOrderValue,
        List<Item> items
) {
    /**
     * @param unitPrice preço do produto somado aos complementos escolhidos
     */
    public record Item(Long productId, String name, BigDecimal unitPrice, int quantity, List<Option> options) {
    }

    public record Option(Long id, String name, BigDecimal price) {
    }
}
