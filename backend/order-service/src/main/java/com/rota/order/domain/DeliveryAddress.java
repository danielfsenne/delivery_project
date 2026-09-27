package com.rota.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

@Embeddable
public record DeliveryAddress(
        String street,
        String number,
        String complement,
        String district,
        String city,
        String state,
        @Column(name = "zip_code") String zipCode,
        Double latitude,
        Double longitude
) {
}
