package com.rota.order.domain.cart;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;
import com.rota.order.domain.cart.CartItem.CartOption;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CartTest {

    private static final BigDecimal FEE = new BigDecimal("5.99");
    private static final BigDecimal MIN = new BigDecimal("20.00");

    private static CartItem burger(String id, int qty, List<CartOption> options) {
        BigDecimal price = options.stream().map(CartOption::price).reduce(new BigDecimal("32.90"), BigDecimal::add);
        return new CartItem(id, 1L, "X-Bacon", price, qty, options, null);
    }

    private Cart cartWith(CartItem item) {
        return Cart.empty(7L).add(1L, "Burger House", FEE, MIN, item, false);
    }

    @Test
    void shouldAddItemAndCalculateSubtotal() {
        Cart cart = cartWith(burger("a", 2, List.of()));

        assertThat(cart.restaurantId()).isEqualTo(1L);
        assertThat(cart.subtotal()).isEqualByComparingTo("65.80");
        assertThat(cart.itemCount()).isEqualTo(2);
        assertThat(cart.reachesMinimumOrder()).isTrue();
    }

    @Test
    void shouldMergeSameConfiguration() {
        Cart cart = cartWith(burger("a", 1, List.of()))
                .add(1L, "Burger House", FEE, MIN, burger("b", 2, List.of()), false);

        assertThat(cart.items()).singleElement().satisfies(i -> assertThat(i.quantity()).isEqualTo(3));
    }

    @Test
    void shouldKeepDifferentOptionsAsSeparateLines() {
        CartOption bacon = new CartOption(10L, "Bacon extra", new BigDecimal("5.00"));
        Cart cart = cartWith(burger("a", 1, List.of()))
                .add(1L, "Burger House", FEE, MIN, burger("b", 1, List.of(bacon)), false);

        assertThat(cart.items()).hasSize(2);
        assertThat(cart.subtotal()).isEqualByComparingTo("70.80");
    }

    @Test
    void shouldRefuseItemFromAnotherRestaurantWithoutReplace() {
        Cart cart = cartWith(burger("a", 1, List.of()));
        CartItem sushi = new CartItem("s", 50L, "Temaki", new BigDecimal("29.90"), 1, List.of(), null);

        assertThatThrownBy(() -> cart.add(2L, "Sushi Kento", FEE, MIN, sushi, false))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("Burger House");
    }

    @Test
    void shouldReplaceCartWhenRequested() {
        Cart cart = cartWith(burger("a", 1, List.of()));
        CartItem sushi = new CartItem("s", 50L, "Temaki", new BigDecimal("29.90"), 1, List.of(), null);

        Cart replaced = cart.add(2L, "Sushi Kento", FEE, MIN, sushi, true);

        assertThat(replaced.restaurantId()).isEqualTo(2L);
        assertThat(replaced.items()).extracting(CartItem::name).containsExactly("Temaki");
    }

    @Test
    void shouldRemoveItemWhenQuantityIsZero() {
        Cart cart = cartWith(burger("a", 1, List.of())).updateQuantity("a", 0);

        assertThat(cart.isEmpty()).isTrue();
        assertThat(cart.restaurantId()).isNull();
    }

    @Test
    void shouldReportMinimumNotReached() {
        Cart cart = Cart.empty(7L).add(1L, "Burger House", FEE, new BigDecimal("100"), burger("a", 1, List.of()),
                false);

        assertThat(cart.reachesMinimumOrder()).isFalse();
    }

    @Test
    void shouldLimitQuantityPerItem() {
        Cart cart = cartWith(burger("a", 49, List.of()));

        assertThatThrownBy(() -> cart.updateQuantity("a", 51)).isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> cart.add(1L, "Burger House", FEE, MIN, burger("b", 2, List.of()), false))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldKeepCouponAcrossChangesAndDropItWhenEmptied() {
        Cart cart = cartWith(burger("a", 1, List.of())).withCoupon("SAVE10").updateQuantity("a", 3);

        assertThat(cart.couponCode()).isEqualTo("SAVE10");
        assertThat(cart.remove("a").couponCode()).isNull();
    }

    @Test
    void shouldFailForUnknownItem() {
        assertThatThrownBy(() -> cartWith(burger("a", 1, List.of())).remove("zzz"))
                .isInstanceOf(NotFoundException.class);
    }
}
