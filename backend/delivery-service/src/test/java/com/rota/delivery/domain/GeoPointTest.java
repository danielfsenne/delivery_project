package com.rota.delivery.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class GeoPointTest {

    @Test
    void distanceToItselfIsZero() {
        GeoPoint franca = new GeoPoint(-20.5386, -47.4009);

        assertThat(franca.distanceKm(franca)).isZero();
    }

    @Test
    void shouldMatchKnownDistanceBetweenCities() {
        GeoPoint saoPaulo = new GeoPoint(-23.5505, -46.6333);
        GeoPoint rioDeJaneiro = new GeoPoint(-22.9068, -43.1729);

        // Distância em linha reta entre os centros das duas cidades: ~361 km
        assertThat(saoPaulo.distanceKm(rioDeJaneiro)).isCloseTo(361, within(2.0));
    }

    @Test
    void shouldBeSymmetric() {
        GeoPoint a = new GeoPoint(-20.5386, -47.4009);
        GeoPoint b = new GeoPoint(-20.5450, -47.4100);

        assertThat(a.distanceKm(b)).isCloseTo(b.distanceKm(a), within(1e-9));
        assertThat(a.distanceKm(b)).isCloseTo(1.19, within(0.05));
    }

    @Test
    void ofNullableRequiresBothCoordinates() {
        assertThat(GeoPoint.ofNullable(null, -47.0)).isNull();
        assertThat(GeoPoint.ofNullable(-20.0, -47.0)).isEqualTo(new GeoPoint(-20.0, -47.0));
    }

    @Test
    void shouldRejectInvalidCoordinates() {
        assertThatThrownBy(() -> new GeoPoint(91, 0)).isInstanceOf(IllegalArgumentException.class);
    }
}
