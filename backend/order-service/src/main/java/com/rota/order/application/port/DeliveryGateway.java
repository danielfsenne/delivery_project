package com.rota.order.application.port;

import com.rota.order.domain.Order;

/**
 * Porta para o delivery-service.
 */
public interface DeliveryGateway {

    /**
     * Abre a corrida para os entregadores. Idempotente por pedido.
     *
     * @throws com.rota.common.exception.ServiceUnavailableException se o serviço de entregas estiver fora do ar
     */
    void requestDelivery(Order order);
}
