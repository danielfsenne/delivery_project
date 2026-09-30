package com.rota.delivery.domain;

public enum DeliveryStatus {
    /** Pedido pronto no restaurante, aguardando um entregador aceitar. */
    WAITING_DRIVER,
    /** Entregador aceitou e está indo ao restaurante. */
    ASSIGNED,
    /** Pedido retirado, a caminho do cliente. */
    PICKED_UP,
    DELIVERED
}
