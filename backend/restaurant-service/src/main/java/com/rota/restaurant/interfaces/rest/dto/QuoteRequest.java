package com.rota.restaurant.interfaces.rest.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * Itens que o order-service quer precificar. Os preços nunca vêm do cliente.
 */
public record QuoteRequest(@NotEmpty List<@Valid Item> items) {

    public record Item(@NotNull Long productId, @Min(1) @Max(50) int quantity, List<Long> optionIds) {
        public List<Long> optionIds() {
            return optionIds == null ? List.of() : optionIds;
        }
    }
}
