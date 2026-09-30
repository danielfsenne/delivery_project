package com.rota.order.application;

import com.rota.common.exception.ForbiddenException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderStatus;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

import static com.rota.order.domain.OrderStatus.CANCELLED;
import static com.rota.order.domain.OrderStatus.CREATED;
import static com.rota.order.domain.OrderStatus.PAID;
import static com.rota.order.domain.OrderStatus.PAYMENT_PENDING;
import static com.rota.order.domain.OrderStatus.PREPARING;
import static com.rota.order.domain.OrderStatus.READY_FOR_PICKUP;
import static com.rota.order.domain.OrderStatus.RESTAURANT_ACCEPTED;

/**
 * Quem pode ver um pedido e quem pode movê-lo para cada status.
 * A validade da transição em si é responsabilidade da máquina de estados ({@link OrderStatus}).
 */
@Component
public class OrderAccessPolicy {

    /** Cliente só cancela antes de o restaurante aceitar. */
    private static final Set<OrderStatus> CUSTOMER_CANCELLABLE = EnumSet.of(CREATED, PAYMENT_PENDING, PAID);

    private static final Set<OrderStatus> RESTAURANT_TARGETS =
            EnumSet.of(RESTAURANT_ACCEPTED, PREPARING, READY_FOR_PICKUP, CANCELLED);

    public void checkCanView(AuthenticatedUser user, Order order) {
        boolean allowed = switch (user.role()) {
            case ADMIN, SERVICE -> true;
            case CUSTOMER -> order.belongsToCustomer(user.id());
            case RESTAURANT -> order.belongsToRestaurantOwner(user.id());
            case DRIVER -> order.isAssignedToDriver(user.id());
        };
        if (!allowed) {
            throw new ForbiddenException("Você não tem acesso a este pedido");
        }
    }

    public void checkCanTransition(AuthenticatedUser user, Order order, OrderStatus target) {
        checkCanView(user, order);
        boolean allowed = switch (user.role()) {
            case ADMIN, SERVICE -> true;
            case CUSTOMER -> target == CANCELLED && CUSTOMER_CANCELLABLE.contains(order.getStatus());
            case RESTAURANT -> RESTAURANT_TARGETS.contains(target);
            // Entregadores avançam o pedido pelo delivery-service, que chama os endpoints internos.
            case DRIVER -> false;
        };
        if (!allowed) {
            throw new ForbiddenException("Você não pode alterar este pedido para " + target);
        }
    }
}
