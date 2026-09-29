package com.rota.order.domain.coupon;

import com.rota.common.exception.BusinessException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CouponTest {

    private static final Instant NOW = Instant.parse("2026-06-01T12:00:00Z");
    private static final Instant TOMORROW = NOW.plusSeconds(86_400);

    private static Coupon save10() {
        return new Coupon(" save10 ", null, CouponType.PERCENTAGE, bd("10"), bd("50"), bd("20"), TOMORROW, null, null);
    }

    private static BigDecimal bd(String v) {
        return new BigDecimal(v);
    }

    @Test
    void shouldNormalizeCode() {
        assertThat(save10().getCode()).isEqualTo("SAVE10");
    }

    @Test
    void shouldApplyPercentage() {
        assertThat(save10().discountFor(bd("80.00"), 1L, NOW)).isEqualByComparingTo("8.00");
    }

    @Test
    void shouldCapPercentageAtMaxDiscount() {
        assertThat(save10().discountFor(bd("500.00"), 1L, NOW)).isEqualByComparingTo("20.00");
    }

    @Test
    void shouldRequireMinimumOrder() {
        assertThatThrownBy(() -> save10().discountFor(bd("49.99"), 1L, NOW))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("pedido mínimo de R$ 50.00");
    }

    @Test
    void shouldRejectExpiredCoupon() {
        assertThatThrownBy(() -> save10().discountFor(bd("80.00"), 1L, TOMORROW.plusSeconds(1)))
                .hasMessageContaining("expirou");
    }

    @Test
    void shouldRejectInactiveCoupon() {
        Coupon coupon = save10();
        coupon.setActive(false);

        assertThatThrownBy(() -> coupon.discountFor(bd("80.00"), 1L, NOW)).hasMessageContaining("não está ativo");
    }

    @Test
    void shouldRejectExhaustedCoupon() {
        Coupon coupon = new Coupon("ONE", null, CouponType.FIXED, bd("5"), null, null, null, 1, null);
        ReflectionTestUtils.setField(coupon, "usedCount", 1);

        assertThatThrownBy(() -> coupon.discountFor(bd("80.00"), 1L, NOW)).hasMessageContaining("esgotado");
    }

    @Test
    void shouldRestrictToRestaurant() {
        Coupon pizza = new Coupon("PIZZA20", null, CouponType.PERCENTAGE, bd("20"), null, null, null, null, 3L);

        assertThat(pizza.discountFor(bd("100"), 3L, NOW)).isEqualByComparingTo("20.00");
        assertThatThrownBy(() -> pizza.discountFor(bd("100"), 1L, NOW)).hasMessageContaining("restaurante");
    }

    @Test
    void fixedDiscountNeverExceedsSubtotal() {
        Coupon fifteen = new Coupon("OFF15", null, CouponType.FIXED, bd("15"), null, null, null, null, null);

        assertThat(fifteen.discountFor(bd("9.90"), 1L, NOW)).isEqualByComparingTo("9.90");
    }

    @Test
    void shouldNotAllowPercentageAboveHundred() {
        assertThatThrownBy(() -> new Coupon("X", null, CouponType.PERCENTAGE, bd("150"), null, null, null, null, null))
                .isInstanceOf(BusinessException.class);
    }
}
