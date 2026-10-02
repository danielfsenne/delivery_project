package com.rota.restaurant.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.messaging.ProcessedEvents;
import com.rota.restaurant.application.cache.EvictsRestaurantCache;
import com.rota.restaurant.domain.RestaurantRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Média de avaliações do restaurante, alimentada pelo evento {@code review.created}.
 * Cada avaliação é contada uma única vez, mesmo que o evento seja entregue de novo.
 */
@Service
public class RatingService {

    static final String CONSUMER = "restaurant.ratings";

    private final RestaurantRepository restaurants;
    private final ProcessedEvents processedEvents;

    public RatingService(RestaurantRepository restaurants, ProcessedEvents processedEvents) {
        this.restaurants = restaurants;
        this.processedEvents = processedEvents;
    }

    /**
     * @return {@code false} se o evento já tinha sido processado
     */
    @Transactional
    @EvictsRestaurantCache
    public boolean addRating(UUID eventId, Long restaurantId, int score) {
        if (score < 1 || score > 5) {
            throw new BusinessException("A nota deve ser de 1 a 5");
        }
        if (!processedEvents.firstDelivery(eventId, CONSUMER)) {
            return false;
        }
        if (restaurants.addRating(restaurantId, score) == 0) {
            throw NotFoundException.of("Restaurante", restaurantId);
        }
        return true;
    }
}
