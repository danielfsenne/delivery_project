package com.rota.order.application;

import com.rota.common.exception.ForbiddenException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrderAccessPolicyTest {

    private static final Long CUSTOMER_ID = 1L;
    private static final Long OWNER_ID = 2L;
    private static final Long DRIVER_ID = 3L;

    private final OrderAccessPolicy policy = new OrderAccessPolicy();

    private static AuthenticatedUser user(Long id, Role role) {
        return new AuthenticatedUser(id, "u" + id + "@rota.dev", role);
    }

    private static Order order(OrderStatus... path) {
        Order order = Order.builder()
                .customer(CUSTOMER_ID)
                .restaurant(10L, "Burger House", OWNER_ID)
                .paymentMethod(PaymentMethod.PIX)
                .item(new OrderItem(1L, "X", null, null, BigDecimal.TEN, 1))
                .build();
        for (OrderStatus status : path) {
            order.transitionTo(status, null, null, Instant.now());
        }
        return order;
    }

    @Test
    void customerCanViewOnlyOwnOrders() {
        Order order = order();

        assertThatCode(() -> policy.checkCanView(user(CUSTOMER_ID, Role.CUSTOMER), order)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.checkCanView(user(99L, Role.CUSTOMER), order))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void restaurantCanViewOnlyOrdersOfOwnRestaurant() {
        Order order = order();

        assertThatCode(() -> policy.checkCanView(user(OWNER_ID, Role.RESTAURANT), order)).doesNotThrowAnyException();
        assertThatThrownBy(() -> policy.checkCanView(user(99L, Role.RESTAURANT), order))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void driverNeedsToBeAssigned() {
        Order order = order();
        assertThatThrownBy(() -> policy.checkCanView(user(DRIVER_ID, Role.DRIVER), order))
                .isInstanceOf(ForbiddenException.class);

        order.assignDriver(DRIVER_ID);
        assertThatCode(() -> policy.checkCanView(user(DRIVER_ID, Role.DRIVER), order)).doesNotThrowAnyException();
    }

    @Test
    void customerCanCancelBeforeRestaurantAccepts() {
        Order paid = order(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID);

        assertThatCode(() -> policy.checkCanTransition(user(CUSTOMER_ID, Role.CUSTOMER), paid, OrderStatus.CANCELLED))
                .doesNotThrowAnyException();
    }

    @Test
    void customerCannotCancelAfterRestaurantAccepts() {
        Order accepted = order(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID, OrderStatus.RESTAURANT_ACCEPTED);

        assertThatThrownBy(() -> policy.checkCanTransition(user(CUSTOMER_ID, Role.CUSTOMER), accepted,
                OrderStatus.CANCELLED)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void customerCannotAdvanceStatus() {
        Order paid = order(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID);

        assertThatThrownBy(() -> policy.checkCanTransition(user(CUSTOMER_ID, Role.CUSTOMER), paid,
                OrderStatus.RESTAURANT_ACCEPTED)).isInstanceOf(ForbiddenException.class);
    }

    @Test
    void restaurantCannotConfirmPaymentOrDeliver() {
        Order order = order(OrderStatus.PAYMENT_PENDING);
        AuthenticatedUser owner = user(OWNER_ID, Role.RESTAURANT);

        assertThatThrownBy(() -> policy.checkCanTransition(owner, order, OrderStatus.PAID))
                .isInstanceOf(ForbiddenException.class);
        assertThatThrownBy(() -> policy.checkCanTransition(owner, order, OrderStatus.DELIVERED))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void restaurantCanAcceptOwnOrder() {
        Order paid = order(OrderStatus.PAYMENT_PENDING, OrderStatus.PAID);

        assertThatCode(() -> policy.checkCanTransition(user(OWNER_ID, Role.RESTAURANT), paid,
                OrderStatus.RESTAURANT_ACCEPTED)).doesNotThrowAnyException();
    }

    @Test
    void adminCanDoAnything() {
        assertThatCode(() -> policy.checkCanTransition(user(99L, Role.ADMIN), order(), OrderStatus.PAID))
                .doesNotThrowAnyException();
    }
}
