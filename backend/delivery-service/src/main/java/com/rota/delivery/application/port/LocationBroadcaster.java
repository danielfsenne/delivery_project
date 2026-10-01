package com.rota.delivery.application.port;

/**
 * Repassa a posição do entregador em uma entrega ativa para quem acompanha o pedido.
 * Melhor esforço: falhas não interrompem a atualização da localização.
 */
public interface LocationBroadcaster {

    void driverMoved(Long driverId, Long orderId, Long customerId, double latitude, double longitude);
}
