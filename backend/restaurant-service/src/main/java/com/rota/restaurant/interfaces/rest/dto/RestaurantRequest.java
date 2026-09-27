package com.rota.restaurant.interfaces.rest.dto;

import com.rota.restaurant.domain.Address;
import com.rota.restaurant.domain.OpeningHour;
import com.rota.restaurant.domain.RestaurantDetails;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record RestaurantRequest(
        @NotBlank @Size(max = 120) String name,
        @Size(max = 500) String description,
        @NotBlank @Size(max = 60) String cuisine,
        @Size(max = 20) String phone,
        @Size(max = 500) String imageUrl,
        @NotNull @DecimalMin("0.00") BigDecimal deliveryFee,
        @DecimalMin("0.00") BigDecimal minOrderValue,
        @Min(1) @Max(240) int deliveryTimeMin,
        @Min(1) @Max(240) int deliveryTimeMax,
        @NotNull @Valid AddressRequest address,
        @NotEmpty List<@Valid OpeningHourRequest> openingHours
) {
    public RestaurantDetails toDetails() {
        return new RestaurantDetails(name.trim(), description, cuisine.trim(), phone, imageUrl, deliveryFee,
                minOrderValue, deliveryTimeMin, deliveryTimeMax, address.toAddress(),
                openingHours.stream().map(OpeningHourRequest::toOpeningHour).toList());
    }

    public record AddressRequest(
            @NotBlank @Size(max = 160) String street,
            @NotBlank @Size(max = 20) String number,
            @Size(max = 80) String complement,
            @NotBlank @Size(max = 80) String district,
            @NotBlank @Size(max = 80) String city,
            @NotBlank @Pattern(regexp = "[A-Z]{2}", message = "UF deve ter 2 letras maiúsculas") String state,
            @NotBlank @Pattern(regexp = "\\d{5}-?\\d{3}", message = "CEP inválido") String zipCode,
            Double latitude,
            Double longitude
    ) {
        Address toAddress() {
            return new Address(street, number, complement, district, city, state, zipCode, latitude, longitude);
        }
    }

    public record OpeningHourRequest(@NotNull DayOfWeek dayOfWeek, @NotNull LocalTime opensAt,
                                     @NotNull LocalTime closesAt) {
        OpeningHour toOpeningHour() {
            return new OpeningHour(dayOfWeek, opensAt, closesAt);
        }
    }
}
