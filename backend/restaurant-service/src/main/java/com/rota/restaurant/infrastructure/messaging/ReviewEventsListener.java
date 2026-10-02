package com.rota.restaurant.infrastructure.messaging;

import com.rota.common.events.ReviewCreated;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventQueues;
import com.rota.restaurant.application.RatingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.Declarables;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ReviewEventsListener {

    static final String REVIEW_QUEUE = "restaurant.review-created";

    private static final Logger log = LoggerFactory.getLogger(ReviewEventsListener.class);

    private final RatingService ratingService;

    public ReviewEventsListener(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @Bean
    Declarables reviewCreatedQueue() {
        return EventQueues.durable(REVIEW_QUEUE, RotaEvents.REVIEW_CREATED);
    }

    @RabbitListener(queues = REVIEW_QUEUE)
    public void onReviewCreated(ReviewCreated event) {
        if (!ratingService.addRating(event.eventId(), event.restaurantId(), event.foodRating())) {
            log.info("Avaliação {} já contabilizada; evento repetido ignorado", event.reviewId());
        }
    }
}
