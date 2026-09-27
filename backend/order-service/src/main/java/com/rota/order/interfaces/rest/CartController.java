package com.rota.order.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.CartService;
import com.rota.order.interfaces.rest.dto.CartDtos.AddItemRequest;
import com.rota.order.interfaces.rest.dto.CartDtos.CartResponse;
import com.rota.order.interfaces.rest.dto.CartDtos.UpdateItemRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse get(@AuthenticationPrincipal AuthenticatedUser user) {
        return CartResponse.from(cartService.get(user.id()));
    }

    @PostMapping("/items")
    public CartResponse addItem(@AuthenticationPrincipal AuthenticatedUser user,
                                @Valid @RequestBody AddItemRequest request) {
        return CartResponse.from(cartService.addItem(user.id(), request));
    }

    @PatchMapping("/items/{itemId}")
    public CartResponse updateItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String itemId,
                                   @Valid @RequestBody UpdateItemRequest request) {
        return CartResponse.from(cartService.updateQuantity(user.id(), itemId, request.quantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable String itemId) {
        return CartResponse.from(cartService.removeItem(user.id(), itemId));
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal AuthenticatedUser user) {
        cartService.clear(user.id());
    }
}
