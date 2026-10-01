package com.rota.restaurant.application.cache;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * Forma serializável de uma página ({@link PageImpl} não é desserializável pelo Jackson).
 */
public record CachedPage<T>(List<T> content, long totalElements) {

    public static <T> CachedPage<T> of(Page<T> page) {
        return new CachedPage<>(page.getContent(), page.getTotalElements());
    }

    public Page<T> toPage(Pageable pageable) {
        return new PageImpl<>(content, pageable, totalElements);
    }
}
