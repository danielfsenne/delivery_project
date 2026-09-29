package com.rota.order.interfaces.rest.dto;

import com.rota.order.domain.cart.Cart;
import com.rota.order.domain.cart.CartItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

public final class CartDtos {

    private CartDtos() {
    }

    public record AddItemRequest(
            @NotNull Long restaurantId,
            @NotNull Long productId,
            @Min(1) @Max(50) int quantity,
            List<Long> optionIds,
            @Size(max = 200) String notes,
            boolean replaceCart
    ) {
        public List<Long> optionIds() {
            return optionIds == null ? List.of() : optionIds;
        }
    }

    public record UpdateItemRequest(@Min(0) @Max(50) int quantity) {
    }

    public record CartResponse(
            Long restaurantId,
            String restaurantName,
            List<CartItem> items,
            int itemCount,
            BigDecimal subtotal,
            BigDecimal deliveryFee,
            BigDecimal discount,
            BigDecimal total,
            BigDecimal minOrderValue,
            boolean reachesMinimumOrder,
            String couponCode,
            String couponError
    ) {
        /**
         * @param discount    desconto do cupom já validado (zero se não houver cupom ou se ele for inválido)
         * @param couponError motivo pelo qual o cupom do carrinho não se aplica no momento
         */
        public static CartResponse from(Cart cart, BigDecimal discount, String couponError) {
            BigDecimal fee = cart.isEmpty() ? BigDecimal.ZERO : cart.deliveryFee();
            BigDecimal total = cart.subtotal().add(fee).subtract(discount).max(BigDecimal.ZERO);
            return new CartResponse(cart.restaurantId(), cart.restaurantName(), cart.items(), cart.itemCount(),
                    cart.subtotal(), fee, discount, total, cart.minOrderValue(), cart.reachesMinimumOrder(),
                    cart.couponCode(), couponError);
        }
    }
}
