package com.rota.order.domain.coupon;

public enum CouponType {
    /** Percentual sobre o subtotal, opcionalmente limitado por {@code maxDiscount}. */
    PERCENTAGE,
    /** Valor fixo em reais. */
    FIXED
}
