package com.rota.delivery.domain;

import com.rota.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "drivers")
public class Driver {

    /** Mesmo id do usuário no auth-service. */
    @Id
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DriverStatus status = DriverStatus.OFFLINE;

    private Double latitude;
    private Double longitude;

    @Column(name = "location_updated_at")
    private Instant locationUpdatedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected Driver() {
    }

    public Driver(Long id, Instant now) {
        this.id = id;
        this.updatedAt = now;
    }

    public void goOnline(Instant now) {
        if (status == DriverStatus.BUSY) {
            throw new BusinessException("Finalize a entrega atual antes de mudar o status");
        }
        status = DriverStatus.ONLINE;
        updatedAt = now;
    }

    public void goOffline(Instant now) {
        if (status == DriverStatus.BUSY) {
            throw new BusinessException("Finalize a entrega atual antes de ficar offline");
        }
        status = DriverStatus.OFFLINE;
        updatedAt = now;
    }

    void startDelivery(Instant now) {
        if (status != DriverStatus.ONLINE) {
            throw new BusinessException("Fique online para aceitar entregas");
        }
        status = DriverStatus.BUSY;
        updatedAt = now;
    }

    void finishDelivery(Instant now) {
        status = DriverStatus.ONLINE;
        updatedAt = now;
    }

    public void moveTo(GeoPoint point, Instant now) {
        this.latitude = point.latitude();
        this.longitude = point.longitude();
        this.locationUpdatedAt = now;
    }

    public GeoPoint location() {
        return GeoPoint.ofNullable(latitude, longitude);
    }

    public Long getId() {
        return id;
    }

    public DriverStatus getStatus() {
        return status;
    }

    public Instant getLocationUpdatedAt() {
        return locationUpdatedAt;
    }
}
