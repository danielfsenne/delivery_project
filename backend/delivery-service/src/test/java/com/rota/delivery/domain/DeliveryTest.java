package com.rota.delivery.domain;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeliveryTest {

    private static final Instant NOW = Instant.parse("2026-01-01T12:00:00Z");

    private Delivery delivery;
    private Driver driver;

    @BeforeEach
    void setUp() {
        delivery = new Delivery(100L, 1L, 10L, "Burger House", "Rua A, 1", new GeoPoint(-20.5386, -47.4009),
                "Rua B, 2", new GeoPoint(-20.5352, -47.4039), new BigDecimal("5.99"), NOW);
        driver = new Driver(3L, NOW);
        driver.goOnline(NOW);
    }

    @Test
    void fullLifecycle() {
        delivery.accept(driver, NOW.plusSeconds(60));
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.ASSIGNED);
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.BUSY);

        delivery.pickUp(driver, NOW.plusSeconds(600));
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.PICKED_UP);

        delivery.complete(driver, NOW.plusSeconds(1200));
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(delivery.getDeliveredAt()).isEqualTo(NOW.plusSeconds(1200));
        assertThat(driver.getStatus()).isEqualTo(DriverStatus.ONLINE);
    }

    @Test
    void offlineDriverCannotAccept() {
        Driver offline = new Driver(4L, NOW);

        assertThatThrownBy(() -> delivery.accept(offline, NOW)).isInstanceOf(BusinessException.class);
        assertThat(delivery.getStatus()).isEqualTo(DeliveryStatus.WAITING_DRIVER);
    }

    @Test
    void cannotBeAcceptedTwice() {
        delivery.accept(driver, NOW);
        Driver other = new Driver(4L, NOW);
        other.goOnline(NOW);

        assertThatThrownBy(() -> delivery.accept(other, NOW)).isInstanceOf(ConflictException.class);
        assertThat(other.getStatus()).isEqualTo(DriverStatus.ONLINE);
    }

    @Test
    void onlyAssignedDriverCanProgress() {
        delivery.accept(driver, NOW);
        Driver other = new Driver(4L, NOW);

        assertThatThrownBy(() -> delivery.pickUp(other, NOW)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void cannotCompleteBeforePickup() {
        delivery.accept(driver, NOW);

        assertThatThrownBy(() -> delivery.complete(driver, NOW)).isInstanceOf(ConflictException.class);
    }

    @Test
    void busyDriverCannotGoOffline() {
        delivery.accept(driver, NOW);

        assertThatThrownBy(() -> driver.goOffline(NOW)).isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldComputeTripDistance() {
        assertThat(delivery.tripDistanceKm()).isBetween(0.4, 0.6);
    }
}
