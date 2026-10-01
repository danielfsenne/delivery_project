package com.rota.restaurant.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.restaurant.application.cache.EvictsRestaurantCache;
import com.rota.restaurant.domain.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RatingService {

    private final RestaurantRepository restaurants;

    public RatingService(RestaurantRepository restaurants) {
        this.restaurants = restaurants;
    }

    @Transactional
    @EvictsRestaurantCache
    public void addRating(Long restaurantId, int score) {
        if (score < 1 || score > 5) {
            throw new BusinessException("A nota deve ser de 1 a 5");
        }
        if (restaurants.addRating(restaurantId, score) == 0) {
            throw NotFoundException.of("Restaurante", restaurantId);
        }
    }
}
