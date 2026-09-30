package com.rota.delivery.interfaces.rest.dto;

import com.rota.delivery.domain.Delivery;
import com.rota.delivery.domain.DeliveryStatus;
import com.rota.delivery.domain.Driver;
import com.rota.delivery.domain.DriverStatus;
import com.rota.delivery.domain.GeoPoint;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;

public final class DeliveryDtos {

    private DeliveryDtos() {
    }

    /** Enviado pelo order-service quando o pedido fica pronto. */
    public record CreateDeliveryRequest(
            @NotNull Long orderId,
            @NotNull Long customerId,
            @NotNull Long restaurantId,
            @NotBlank String restaurantName,
            String pickupAddress,
            Double pickupLatitude,
            Double pickupLongitude,
            @NotBlank String dropoffAddress,
            Double dropoffLatitude,
            Double dropoffLongitude,
            @NotNull BigDecimal deliveryFee
    ) {
    }

    public record StatusRequest(@NotNull DriverStatus status) {
    }

    public record LocationRequest(
            @NotNull @DecimalMin("-90") @DecimalMax("90") Double latitude,
            @NotNull @DecimalMin("-180") @DecimalMax("180") Double longitude
    ) {
    }

    public record Coordinates(double latitude, double longitude) {
        static Coordinates from(GeoPoint point) {
            return point == null ? null : new Coordinates(point.latitude(), point.longitude());
        }
    }

    /**
     * @param distanceToPickupKm distância do entregador até o restaurante (quando ambos têm coordenadas)
     * @param tripDistanceKm     distância do restaurante até o cliente
     */
    public record DeliveryResponse(
            Long id,
            Long orderId,
            Long restaurantId,
            String restaurantName,
            String pickupAddress,
            Coordinates pickup,
            String dropoffAddress,
            Coordinates dropoff,
            BigDecimal driverFee,
            DeliveryStatus status,
            Long driverId,
            Double distanceToPickupKm,
            Double tripDistanceKm,
            Instant createdAt,
            Instant acceptedAt,
            Instant pickedUpAt,
            Instant deliveredAt
    ) {
        public static DeliveryResponse from(Delivery d, GeoPoint driverLocation) {
            GeoPoint pickup = d.pickup();
            Double toPickup = driverLocation == null || pickup == null ? null : round(driverLocation.distanceKm(pickup));
            Double trip = d.tripDistanceKm() == null ? null : round(d.tripDistanceKm());
            return new DeliveryResponse(d.getId(), d.getOrderId(), d.getRestaurantId(), d.getRestaurantName(),
                    d.getPickupAddress(), Coordinates.from(pickup), d.getDropoffAddress(),
                    Coordinates.from(d.dropoff()), d.getDriverFee(), d.getStatus(),
                    d.getDriver() == null ? null : d.getDriver().getId(), toPickup, trip, d.getCreatedAt(),
                    d.getAcceptedAt(), d.getPickedUpAt(), d.getDeliveredAt());
        }

        private static double round(double km) {
            return BigDecimal.valueOf(km).setScale(1, RoundingMode.HALF_UP).doubleValue();
        }
    }

    public record DriverResponse(
            Long id,
            DriverStatus status,
            Coordinates location,
            Instant locationUpdatedAt,
            DeliveryResponse currentDelivery,
            long completedDeliveries,
            BigDecimal totalEarnings
    ) {
        public static DriverResponse from(Driver driver, Delivery current, long completed, BigDecimal earnings) {
            return new DriverResponse(driver.getId(), driver.getStatus(), Coordinates.from(driver.location()),
                    driver.getLocationUpdatedAt(),
                    current == null ? null : DeliveryResponse.from(current, driver.location()),
                    completed, earnings);
        }
    }

    /** Visão do cliente acompanhando o pedido. */
    public record TrackingResponse(Long orderId, DeliveryStatus status, Coordinates driverLocation,
                                   Instant driverLocationUpdatedAt, Coordinates pickup, Coordinates dropoff) {
        public static TrackingResponse from(Delivery d) {
            Driver driver = d.getDriver();
            boolean showDriver = driver != null && d.getStatus() != DeliveryStatus.DELIVERED;
            return new TrackingResponse(d.getOrderId(), d.getStatus(),
                    showDriver ? Coordinates.from(driver.location()) : null,
                    showDriver ? driver.getLocationUpdatedAt() : null,
                    Coordinates.from(d.pickup()), Coordinates.from(d.dropoff()));
        }
    }

    public record HistoryResponse(long completedDeliveries, BigDecimal totalEarnings,
                                  List<DeliveryResponse> deliveries) {
    }
}
