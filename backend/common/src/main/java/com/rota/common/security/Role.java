package com.rota.common.security;

public enum Role {
    CUSTOMER,
    RESTAURANT,
    DRIVER,
    ADMIN,
    /** Uso exclusivo em chamadas entre serviços; não pode ser atribuído a usuários. */
    SERVICE;

    public boolean isSelfRegistrable() {
        return this == CUSTOMER || this == RESTAURANT || this == DRIVER;
    }
}
