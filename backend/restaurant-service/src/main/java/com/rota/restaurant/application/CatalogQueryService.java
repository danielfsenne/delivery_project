package com.rota.restaurant.application;

import com.rota.common.exception.NotFoundException;
import com.rota.restaurant.application.cache.CachedPage;
import com.rota.restaurant.application.cache.CatalogCaches;
import com.rota.restaurant.domain.Restaurant;
import com.rota.restaurant.domain.RestaurantRepository;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

/**
 * Consultas públicas do catálogo, com cache no Redis.
 * O campo "aberto agora" pode ficar até um TTL desatualizado; não há risco, porque a
 * cotação usada no carrinho e no checkout não passa pelo cache e recusa restaurante fechado.
 */
@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

    private final RestaurantRepository restaurants;
    private final BusinessClock clock;

    public CatalogQueryService(RestaurantRepository restaurants, BusinessClock clock) {
        this.restaurants = restaurants;
        this.clock = clock;
    }

    @Cacheable(cacheNames = CatalogCaches.SEARCH,
            key = "T(com.rota.restaurant.application.CatalogQueryService).searchKey(#city, #cuisine, #term, #pageable)")
    public CachedPage<RestaurantSummaryResponse> search(String city, String cuisine, String term, Pageable pageable) {
        return CachedPage.of(restaurants.search(normalize(city), normalize(cuisine), normalize(term), pageable)
                .map(r -> RestaurantSummaryResponse.from(r, r.isOpenAt(clock.now()))));
    }

    @Cacheable(cacheNames = CatalogCaches.DETAIL, key = "#id")
    public RestaurantDetailResponse detail(Long id) {
        Restaurant restaurant = restaurants.findById(id)
                .filter(Restaurant::isActive)
                .orElseThrow(() -> NotFoundException.of("Restaurante", id));
        return RestaurantDetailResponse.from(restaurant, restaurant.isOpenAt(clock.now()));
    }

    /** Buscas equivalentes ("Franca" e " franca ") compartilham a mesma entrada no cache. */
    public static String searchKey(String city, String cuisine, String term, Pageable pageable) {
        return String.join("|", normalize(city), normalize(cuisine), normalize(term),
                String.valueOf(pageable.getPageNumber()), String.valueOf(pageable.getPageSize()),
                pageable.getSort().toString());
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
