package com.rota.delivery.domain;

/**
 * Coordenada geográfica em graus decimais.
 */
public record GeoPoint(double latitude, double longitude) {

    private static final double EARTH_RADIUS_KM = 6371.0088;

    public GeoPoint {
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Coordenada inválida: " + latitude + ", " + longitude);
        }
    }

    /** Cria o ponto apenas se as duas coordenadas existirem. */
    public static GeoPoint ofNullable(Double latitude, Double longitude) {
        return latitude == null || longitude == null ? null : new GeoPoint(latitude, longitude);
    }

    /**
     * Distância em linha reta pela fórmula de Haversine. Suficiente para ordenar entregas
     * próximas; o tempo real de rota depende de uma API de mapas.
     */
    public double distanceKm(GeoPoint other) {
        double dLat = Math.toRadians(other.latitude - latitude);
        double dLon = Math.toRadians(other.longitude - longitude);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(latitude)) * Math.cos(Math.toRadians(other.latitude))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        return 2 * EARTH_RADIUS_KM * Math.asin(Math.sqrt(a));
    }
}
