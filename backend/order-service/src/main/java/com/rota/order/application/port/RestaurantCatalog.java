package com.rota.order.application.port;

import com.rota.order.domain.DeliveryAddress;

import java.math.BigDecimal;
import java.util.List;

/**
 * Porta para o catálogo de restaurantes. A implementação real chama o restaurant-service;
 * nos testes é substituída por um dublê.
 */
public interface RestaurantCatalog {

    Quote quote(Long restaurantId, List<QuoteLine> lines);

    /** Registra a nota de uma avaliação na média do restaurante. */
    void addRating(Long restaurantId, int score);

    record QuoteLine(Long productId, int quantity, List<Long> optionIds) {
    }

    record Quote(Long restaurantId, String restaurantName, Long ownerId, boolean open,
                 BigDecimal deliveryFee, BigDecimal minOrderValue, DeliveryAddress address,
                 List<QuotedItem> items) {
    }

    record QuotedItem(Long productId, String name, BigDecimal unitPrice, int quantity, List<QuotedOption> options) {
    }

    record QuotedOption(Long id, String name, BigDecimal price) {
    }
}
