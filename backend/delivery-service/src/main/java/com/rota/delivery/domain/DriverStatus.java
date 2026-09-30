package com.rota.delivery.domain;

public enum DriverStatus {
    ONLINE,
    OFFLINE,
    /** Em uma entrega; definido pelo sistema, não pelo entregador. */
    BUSY
}
