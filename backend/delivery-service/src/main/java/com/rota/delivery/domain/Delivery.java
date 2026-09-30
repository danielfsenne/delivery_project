package com.rota.delivery.domain;

import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Corrida de entrega de um pedido. O {@link Version} impede que dois entregadores
 * aceitem a mesma entrega ao mesmo tempo.
 */
@Entity
@Table(name = "deliveries")
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(name = "pickup_longitude")
    private Double pickupLongitude;

    @Column(name = "dropoff_address", nullable = false)
    private String dropoffAddress;

    @Column(name = "dropoff_latitude")
    private Double dropoffLatitude;

    @Column(name = "dropoff_longitude")
    private Double dropoffLongitude;

    @Column(name = "driver_fee", nullable = false)
    private BigDecimal driverFee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DeliveryStatus status = DeliveryStatus.WAITING_DRIVER;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "driver_id")
    private Driver driver;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "accepted_at")
    private Instant acceptedAt;

    @Column(name = "picked_up_at")
    private Instant pickedUpAt;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    @Version
    private long version;

    protected Delivery() {
    }

    public Delivery(Long orderId, Long customerId, Long restaurantId, String restaurantName,
                    String pickupAddress, GeoPoint pickup, String dropoffAddress, GeoPoint dropoff,
                    BigDecimal driverFee, Instant now) {
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.restaurantName = restaurantName;
        this.pickupAddress = pickupAddress;
        this.pickupLatitude = pickup == null ? null : pickup.latitude();
        this.pickupLongitude = pickup == null ? null : pickup.longitude();
        this.dropoffAddress = dropoffAddress;
        this.dropoffLatitude = dropoff == null ? null : dropoff.latitude();
        this.dropoffLongitude = dropoff == null ? null : dropoff.longitude();
        this.driverFee = driverFee;
        this.createdAt = now;
    }

    public void accept(Driver driver, Instant now) {
        if (status != DeliveryStatus.WAITING_DRIVER) {
            throw new ConflictException("Esta entrega já foi aceita por outro entregador");
        }
        driver.startDelivery(now);
        this.driver = driver;
        this.status = DeliveryStatus.ASSIGNED;
        this.acceptedAt = now;
    }

    public void pickUp(Driver driver, Instant now) {
        requireDriver(driver);
        requireStatus(DeliveryStatus.ASSIGNED, "Só é possível retirar uma entrega aceita");
        this.status = DeliveryStatus.PICKED_UP;
        this.pickedUpAt = now;
    }

    public void complete(Driver driver, Instant now) {
        requireDriver(driver);
        requireStatus(DeliveryStatus.PICKED_UP, "Retire o pedido no restaurante antes de concluir a entrega");
        this.status = DeliveryStatus.DELIVERED;
        this.deliveredAt = now;
        driver.finishDelivery(now);
    }

    public boolean isAssignedTo(Long driverId) {
        return driver != null && driver.getId().equals(driverId);
    }

    private void requireDriver(Driver actor) {
        if (!isAssignedTo(actor.getId())) {
            throw new ForbiddenException("Esta entrega não é sua");
        }
    }

    private void requireStatus(DeliveryStatus expected, String message) {
        if (status != expected) {
            throw new ConflictException(message);
        }
    }

    public GeoPoint pickup() {
        return GeoPoint.ofNullable(pickupLatitude, pickupLongitude);
    }

    public GeoPoint dropoff() {
        return GeoPoint.ofNullable(dropoffLatitude, dropoffLongitude);
    }

    /** Distância restaurante -> cliente, se as duas coordenadas forem conhecidas. */
    public Double tripDistanceKm() {
        GeoPoint from = pickup();
        GeoPoint to = dropoff();
        return from == null || to == null ? null : from.distanceKm(to);
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public String getDropoffAddress() {
        return dropoffAddress;
    }

    public BigDecimal getDriverFee() {
        return driverFee;
    }

    public DeliveryStatus getStatus() {
        return status;
    }

    public Driver getDriver() {
        return driver;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getAcceptedAt() {
        return acceptedAt;
    }

    public Instant getPickedUpAt() {
        return pickedUpAt;
    }

    public Instant getDeliveredAt() {
        return deliveredAt;
    }
}
