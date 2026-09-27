package com.rota.restaurant.domain;

import com.rota.common.exception.BusinessException;

import java.math.BigDecimal;
import java.util.List;

/**
 * Dados editáveis de um restaurante, validados antes de chegar à entidade.
 */
public record RestaurantDetails(
        String name,
        String description,
        String cuisine,
        String phone,
        String imageUrl,
        BigDecimal deliveryFee,
        BigDecimal minOrderValue,
        int deliveryTimeMin,
        int deliveryTimeMax,
        Address address,
        List<OpeningHour> openingHours
) {
    public RestaurantDetails {
        if (deliveryTimeMin > deliveryTimeMax) {
            throw new BusinessException("Tempo mínimo de entrega não pode ser maior que o máximo");
        }
        openingHours = openingHours == null ? List.of() : List.copyOf(openingHours);
        minOrderValue = minOrderValue == null ? BigDecimal.ZERO : minOrderValue;
    }
}
