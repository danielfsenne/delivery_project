package com.rota.order.domain.cart;

import java.util.Optional;

public interface CartRepository {

    Optional<Cart> findByUserId(Long userId);

    void save(Cart cart);

    void deleteByUserId(Long userId);
}
