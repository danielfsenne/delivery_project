package com.rota.delivery.application.port;

/**
 * Porta para o order-service: mantém o pedido em sincronia com a corrida.
 */
public interface OrderGateway {

    void assignDriver(Long orderId, Long driverId);

    void markOutForDelivery(Long orderId, Long driverId);

    void markDelivered(Long orderId, Long driverId);
}
