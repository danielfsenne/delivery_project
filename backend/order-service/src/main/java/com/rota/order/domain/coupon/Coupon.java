package com.rota.order.domain.coupon;

import com.rota.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;

@Entity
@Table(name = "coupons")
public class Coupon {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CouponType type;

    @Column(nullable = false)
    private BigDecimal value;

    @Column(name = "min_order_value", nullable = false)
    private BigDecimal minOrderValue = BigDecimal.ZERO;

    @Column(name = "max_discount")
    private BigDecimal maxDiscount;

    @Column(name = "valid_until")
    private Instant validUntil;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "used_count", nullable = false)
    private int usedCount;

    @Column(name = "restaurant_id")
    private Long restaurantId;

    private boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Coupon() {
    }

    public Coupon(String code, String description, CouponType type, BigDecimal value, BigDecimal minOrderValue,
                  BigDecimal maxDiscount, Instant validUntil, Integer usageLimit, Long restaurantId) {
        if (type == CouponType.PERCENTAGE && value.compareTo(HUNDRED) > 0) {
            throw new BusinessException("Percentual de desconto não pode passar de 100%");
        }
        this.code = normalize(code);
        this.description = description;
        this.type = type;
        this.value = value;
        this.minOrderValue = minOrderValue == null ? BigDecimal.ZERO : minOrderValue;
        this.maxDiscount = maxDiscount;
        this.validUntil = validUntil;
        this.usageLimit = usageLimit;
        this.restaurantId = restaurantId;
    }

    public static String normalize(String code) {
        return code == null ? null : code.trim().toUpperCase(Locale.ROOT);
    }

    /**
     * Calcula o desconto para um subtotal, validando todas as regras do cupom.
     * O desconto nunca passa do próprio subtotal.
     *
     * @throws BusinessException com o motivo, se o cupom não puder ser usado
     */
    public BigDecimal discountFor(BigDecimal subtotal, Long orderRestaurantId, Instant now) {
        if (!active) {
            throw new BusinessException("Cupom " + code + " não está ativo");
        }
        if (validUntil != null && now.isAfter(validUntil)) {
            throw new BusinessException("Cupom " + code + " expirou");
        }
        if (usageLimit != null && usedCount >= usageLimit) {
            throw new BusinessException("Cupom " + code + " esgotado");
        }
        if (restaurantId != null && !restaurantId.equals(orderRestaurantId)) {
            throw new BusinessException("Cupom " + code + " não é válido para este restaurante");
        }
        if (subtotal.compareTo(minOrderValue) < 0) {
            throw new BusinessException("Cupom " + code + " exige pedido mínimo de R$ "
                    + minOrderValue.setScale(2, RoundingMode.HALF_EVEN));
        }

        BigDecimal discount = type == CouponType.PERCENTAGE
                ? subtotal.multiply(value).divide(HUNDRED, 2, RoundingMode.HALF_EVEN)
                : value;
        if (maxDiscount != null) {
            discount = discount.min(maxDiscount);
        }
        return discount.min(subtotal).setScale(2, RoundingMode.HALF_EVEN);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public CouponType getType() {
        return type;
    }

    public BigDecimal getValue() {
        return value;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public BigDecimal getMaxDiscount() {
        return maxDiscount;
    }

    public Instant getValidUntil() {
        return validUntil;
    }

    public Integer getUsageLimit() {
        return usageLimit;
    }

    public int getUsedCount() {
        return usedCount;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public boolean isActive() {
        return active;
    }
}
