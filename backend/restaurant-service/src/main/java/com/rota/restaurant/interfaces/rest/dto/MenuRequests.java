package com.rota.restaurant.interfaces.rest.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Requisições de gestão do cardápio.
 */
public final class MenuRequests {

    private MenuRequests() {
    }

    public record CategoryRequest(@NotBlank @Size(max = 80) String name, @Min(0) Integer position) {
    }

    public record ProductRequest(
            @NotBlank @Size(max = 120) String name,
            @Size(max = 500) String description,
            @NotNull @DecimalMin("0.00") BigDecimal price,
            @Size(max = 500) String imageUrl
    ) {
    }

    public record OptionRequest(@NotBlank @Size(max = 80) String name, @NotNull @DecimalMin("0.00") BigDecimal price) {
    }

    public record AvailabilityRequest(@NotNull Boolean available) {
    }

    public record StatusRequest(@NotNull Boolean active) {
    }
}
