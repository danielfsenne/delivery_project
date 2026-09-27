package com.rota.order.domain;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados do pedido. As únicas transições possíveis são as declaradas
 * em {@link #TRANSITIONS}; qualquer outra é rejeitada pelo domínio.
 *
 * <pre>
 * CREATED -> PAYMENT_PENDING -> PAID -> RESTAURANT_ACCEPTED -> PREPARING
 *         -> READY_FOR_PICKUP -> OUT_FOR_DELIVERY -> DELIVERED
 *
 * CANCELLED é alcançável até o restaurante começar o preparo.
 * </pre>
 */
public enum OrderStatus {
    CREATED,
    PAYMENT_PENDING,
    PAID,
    RESTAURANT_ACCEPTED,
    PREPARING,
    READY_FOR_PICKUP,
    OUT_FOR_DELIVERY,
    DELIVERED,
    CANCELLED;

    private static final Map<OrderStatus, Set<OrderStatus>> TRANSITIONS = new EnumMap<>(OrderStatus.class);

    static {
        TRANSITIONS.put(CREATED, EnumSet.of(PAYMENT_PENDING, CANCELLED));
        TRANSITIONS.put(PAYMENT_PENDING, EnumSet.of(PAID, CANCELLED));
        TRANSITIONS.put(PAID, EnumSet.of(RESTAURANT_ACCEPTED, CANCELLED));
        TRANSITIONS.put(RESTAURANT_ACCEPTED, EnumSet.of(PREPARING, CANCELLED));
        TRANSITIONS.put(PREPARING, EnumSet.of(READY_FOR_PICKUP));
        TRANSITIONS.put(READY_FOR_PICKUP, EnumSet.of(OUT_FOR_DELIVERY));
        TRANSITIONS.put(OUT_FOR_DELIVERY, EnumSet.of(DELIVERED));
        TRANSITIONS.put(DELIVERED, EnumSet.noneOf(OrderStatus.class));
        TRANSITIONS.put(CANCELLED, EnumSet.noneOf(OrderStatus.class));
    }

    public boolean canTransitionTo(OrderStatus target) {
        return TRANSITIONS.get(this).contains(target);
    }

    public Set<OrderStatus> nextStatuses() {
        return Collections.unmodifiableSet(TRANSITIONS.get(this));
    }

    public boolean isFinal() {
        return TRANSITIONS.get(this).isEmpty();
    }
}
