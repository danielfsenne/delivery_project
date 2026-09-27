package com.rota.order.domain.cart;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;

/**
 * @param id        identificador da linha no carrinho (o mesmo produto pode aparecer com complementos diferentes)
 * @param unitPrice preço do produto somado aos complementos, conforme cotado no catálogo
 */
public record CartItem(
        String id,
        Long productId,
        String name,
        BigDecimal unitPrice,
        int quantity,
        List<CartOption> options,
        String notes
) {
    public CartItem {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public List<Long> optionIds() {
        return options.stream().map(CartOption::id).sorted().toList();
    }

    /** Mesma configuração de produto: pode ser somada em uma única linha. */
    public boolean sameConfigurationAs(CartItem other) {
        return productId.equals(other.productId)
                && optionIds().equals(other.optionIds())
                && Objects.equals(normalize(notes), normalize(other.notes));
    }

    public CartItem withQuantity(int newQuantity) {
        return new CartItem(id, productId, name, unitPrice, newQuantity, options, notes);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    public record CartOption(Long id, String name, BigDecimal price) {
    }
}
