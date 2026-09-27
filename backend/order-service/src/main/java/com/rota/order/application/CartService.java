package com.rota.order.application;

import com.rota.order.application.port.RestaurantCatalog;
import com.rota.order.application.port.RestaurantCatalog.Quote;
import com.rota.order.application.port.RestaurantCatalog.QuoteLine;
import com.rota.order.application.port.RestaurantCatalog.QuotedItem;
import com.rota.order.domain.cart.Cart;
import com.rota.order.domain.cart.CartItem;
import com.rota.order.domain.cart.CartRepository;
import com.rota.order.interfaces.rest.dto.CartDtos.AddItemRequest;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class CartService {

    private final CartRepository carts;
    private final RestaurantCatalog catalog;

    public CartService(CartRepository carts, RestaurantCatalog catalog) {
        this.carts = carts;
        this.catalog = catalog;
    }

    public Cart get(Long userId) {
        return carts.findByUserId(userId).orElseGet(() -> Cart.empty(userId));
    }

    /**
     * Preço e nome vêm sempre da cotação do catálogo, nunca do cliente.
     */
    public Cart addItem(Long userId, AddItemRequest request) {
        Quote quote = catalog.quote(request.restaurantId(),
                List.of(new QuoteLine(request.productId(), request.quantity(), request.optionIds())));
        QuotedItem quoted = quote.items().getFirst();
        CartItem item = new CartItem(UUID.randomUUID().toString(), quoted.productId(), quoted.name(),
                quoted.unitPrice(), quoted.quantity(),
                quoted.options().stream().map(o -> new CartItem.CartOption(o.id(), o.name(), o.price())).toList(),
                blankToNull(request.notes()));

        Cart updated = get(userId).add(quote.restaurantId(), quote.restaurantName(), quote.deliveryFee(),
                quote.minOrderValue(), item, request.replaceCart());
        carts.save(updated);
        return updated;
    }

    public Cart updateQuantity(Long userId, String itemId, int quantity) {
        return persist(get(userId).updateQuantity(itemId, quantity));
    }

    public Cart removeItem(Long userId, String itemId) {
        return persist(get(userId).remove(itemId));
    }

    public void clear(Long userId) {
        carts.deleteByUserId(userId);
    }

    private Cart persist(Cart cart) {
        if (cart.isEmpty()) {
            carts.deleteByUserId(cart.userId());
        } else {
            carts.save(cart);
        }
        return cart;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
