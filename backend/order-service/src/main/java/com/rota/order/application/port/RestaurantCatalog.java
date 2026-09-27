package com.rota.order.application.port;

import java.math.BigDecimal;
import java.util.List;

/**
 * Porta para o catálogo de restaurantes. A implementação real chama o restaurant-service;
 * nos testes é substituída por um dublê.
 */
public interface RestaurantCatalog {

    Quote quote(Long restaurantId, List<QuoteLine> lines);

    record QuoteLine(Long productId, int quantity, List<Long> optionIds) {
    }

    record Quote(Long restaurantId, String restaurantName, Long ownerId, boolean open,
                 BigDecimal deliveryFee, BigDecimal minOrderValue, List<QuotedItem> items) {
    }

    record QuotedItem(Long productId, String name, BigDecimal unitPrice, int quantity, List<QuotedOption> options) {
    }

    record QuotedOption(Long id, String name, BigDecimal price) {
    }
}
