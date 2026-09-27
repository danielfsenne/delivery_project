package com.rota.order.infrastructure.persistence;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.order.domain.cart.Cart;
import com.rota.order.domain.cart.CartRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.Optional;

/**
 * Carrinho em Redis como JSON, com expiração renovada a cada alteração.
 */
@Repository
public class RedisCartRepository implements CartRepository {

    private final StringRedisTemplate redis;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public RedisCartRepository(StringRedisTemplate redis, ObjectMapper objectMapper,
                               @Value("${rota.cart.ttl:2d}") Duration ttl) {
        this.redis = redis;
        this.objectMapper = objectMapper;
        this.ttl = ttl;
    }

    @Override
    public Optional<Cart> findByUserId(Long userId) {
        return Optional.ofNullable(redis.opsForValue().get(key(userId))).map(this::read);
    }

    @Override
    public void save(Cart cart) {
        redis.opsForValue().set(key(cart.userId()), write(cart), ttl);
    }

    @Override
    public void deleteByUserId(Long userId) {
        redis.delete(key(userId));
    }

    static String key(Long userId) {
        return "cart:user:" + userId;
    }

    private Cart read(String json) {
        try {
            return objectMapper.readValue(json, Cart.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Carrinho corrompido no Redis", e);
        }
    }

    private String write(Cart cart) {
        try {
            return objectMapper.writeValueAsString(cart);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Falha ao serializar carrinho", e);
        }
    }
}
