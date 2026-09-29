package com.rota.order.interfaces.rest.dto;

import com.rota.order.domain.coupon.Coupon;
import com.rota.order.domain.coupon.CouponType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.Instant;

public final class CouponDtos {

    private CouponDtos() {
    }

    public record ApplyCouponRequest(@NotBlank @Size(max = 30) String code) {
    }

    public record CouponRequest(
            @NotBlank @Pattern(regexp = "[A-Za-z0-9_-]{3,30}", message = "Use de 3 a 30 letras, números, - ou _")
            String code,
            @Size(max = 200) String description,
            @NotNull CouponType type,
            @NotNull @DecimalMin(value = "0.01") BigDecimal value,
            @DecimalMin("0.00") BigDecimal minOrderValue,
            @DecimalMin("0.01") BigDecimal maxDiscount,
            Instant validUntil,
            @Min(1) Integer usageLimit,
            Long restaurantId
    ) {
    }

    public record CouponStatusRequest(@NotNull Boolean active) {
    }

    public record CouponResponse(Long id, String code, String description, CouponType type, BigDecimal value,
                                 BigDecimal minOrderValue, BigDecimal maxDiscount, Instant validUntil,
                                 Integer usageLimit, int usedCount, Long restaurantId, boolean active) {
        public static CouponResponse from(Coupon c) {
            return new CouponResponse(c.getId(), c.getCode(), c.getDescription(), c.getType(), c.getValue(),
                    c.getMinOrderValue(), c.getMaxDiscount(), c.getValidUntil(), c.getUsageLimit(),
                    c.getUsedCount(), c.getRestaurantId(), c.isActive());
        }
    }
}
