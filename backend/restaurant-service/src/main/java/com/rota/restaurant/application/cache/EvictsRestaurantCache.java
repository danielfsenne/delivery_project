package com.rota.restaurant.application.cache;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marca métodos que alteram um restaurante: remove o detalhe dele e todas as páginas de busca.
 * O método precisa ter um parâmetro chamado {@code restaurantId}.
 * Como o cache é transacional, a remoção só acontece depois do commit.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Caching(evict = {
        @CacheEvict(cacheNames = CatalogCaches.DETAIL, key = "#restaurantId"),
        @CacheEvict(cacheNames = CatalogCaches.SEARCH, allEntries = true)
})
public @interface EvictsRestaurantCache {
}
