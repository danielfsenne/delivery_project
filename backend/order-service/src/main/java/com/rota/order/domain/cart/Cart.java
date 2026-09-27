package com.rota.order.domain.cart;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * Carrinho de um usuário. Imutável: cada operação devolve um novo carrinho.
 * Só contém itens de um restaurante por vez.
 */
public record Cart(
        Long userId,
        Long restaurantId,
        String restaurantName,
        BigDecimal deliveryFee,
        BigDecimal minOrderValue,
        List<CartItem> items
) {
    public static final int MAX_QUANTITY_PER_ITEM = 50;

    public Cart {
        items = items == null ? List.of() : List.copyOf(items);
    }

    public static Cart empty(Long userId) {
        return new Cart(userId, null, null, BigDecimal.ZERO, BigDecimal.ZERO, List.of());
    }

    @JsonIgnore
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Adiciona um item. Se o carrinho tem itens de outro restaurante, exige {@code replace}
     * para descartá-los; itens com a mesma configuração têm as quantidades somadas.
     */
    public Cart add(Long restaurantId, String restaurantName, BigDecimal deliveryFee, BigDecimal minOrderValue,
                    CartItem item, boolean replace) {
        boolean otherRestaurant = !isEmpty() && !restaurantId.equals(this.restaurantId);
        if (otherRestaurant && !replace) {
            throw new ConflictException("O carrinho já tem itens de " + this.restaurantName
                    + ". Esvazie o carrinho para pedir de outro restaurante.");
        }
        List<CartItem> newItems = new ArrayList<>(otherRestaurant ? List.of() : items);
        int existing = indexOfSameConfiguration(newItems, item);
        if (existing >= 0) {
            CartItem current = newItems.get(existing);
            newItems.set(existing, current.withQuantity(checkQuantity(current.quantity() + item.quantity())));
        } else {
            checkQuantity(item.quantity());
            newItems.add(item);
        }
        return new Cart(userId, restaurantId, restaurantName, deliveryFee, minOrderValue, newItems);
    }

    public Cart updateQuantity(String itemId, int quantity) {
        if (quantity <= 0) {
            return remove(itemId);
        }
        checkQuantity(quantity);
        List<CartItem> newItems = items.stream()
                .map(i -> i.id().equals(itemId) ? i.withQuantity(quantity) : i)
                .toList();
        requireItem(itemId);
        return new Cart(userId, restaurantId, restaurantName, deliveryFee, minOrderValue, newItems);
    }

    public Cart remove(String itemId) {
        requireItem(itemId);
        List<CartItem> newItems = items.stream().filter(i -> !i.id().equals(itemId)).toList();
        return newItems.isEmpty()
                ? empty(userId)
                : new Cart(userId, restaurantId, restaurantName, deliveryFee, minOrderValue, newItems);
    }

    public BigDecimal subtotal() {
        return items.stream()
                .map(CartItem::totalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_EVEN);
    }

    public int itemCount() {
        return items.stream().mapToInt(CartItem::quantity).sum();
    }

    public boolean reachesMinimumOrder() {
        return subtotal().compareTo(minOrderValue) >= 0;
    }

    private void requireItem(String itemId) {
        if (items.stream().noneMatch(i -> i.id().equals(itemId))) {
            throw NotFoundException.of("Item do carrinho", itemId);
        }
    }

    private static int indexOfSameConfiguration(List<CartItem> list, CartItem item) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).sameConfigurationAs(item)) {
                return i;
            }
        }
        return -1;
    }

    private static int checkQuantity(int quantity) {
        if (quantity > MAX_QUANTITY_PER_ITEM) {
            throw new BusinessException("Quantidade máxima por item é " + MAX_QUANTITY_PER_ITEM);
        }
        return quantity;
    }
}
