package com.rota.order.infrastructure.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "restaurant-service-internal", url = "${rota.clients.restaurant-url}",
        configuration = InternalFeignConfig.class)
public interface RestaurantInternalFeignClient {

    @PostMapping("/internal/restaurants/{id}/ratings")
    void addRating(@PathVariable("id") Long restaurantId, @RequestBody RatingBody body);

    record RatingBody(int score) {
    }
}
