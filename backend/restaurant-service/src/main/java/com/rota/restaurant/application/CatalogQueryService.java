package com.rota.restaurant.application;

import com.rota.common.exception.NotFoundException;
import com.rota.restaurant.domain.Restaurant;
import com.rota.restaurant.domain.RestaurantRepository;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class CatalogQueryService {

    private final RestaurantRepository restaurants;
    private final BusinessClock clock;

    public CatalogQueryService(RestaurantRepository restaurants, BusinessClock clock) {
        this.restaurants = restaurants;
        this.clock = clock;
    }

    public Page<RestaurantSummaryResponse> search(String city, String cuisine, String term, Pageable pageable) {
        return restaurants.search(normalize(city), normalize(cuisine), normalize(term), pageable)
                .map(r -> RestaurantSummaryResponse.from(r, r.isOpenAt(clock.now())));
    }

    public RestaurantDetailResponse detail(Long id) {
        Restaurant restaurant = restaurants.findById(id)
                .filter(Restaurant::isActive)
                .orElseThrow(() -> NotFoundException.of("Restaurante", id));
        return RestaurantDetailResponse.from(restaurant, restaurant.isOpenAt(clock.now()));
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
