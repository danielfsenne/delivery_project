package com.rota.order.infrastructure.client;

import com.rota.common.feign.InternalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "restaurant-service", contextId = "restaurantInternal",
        configuration = InternalFeignConfig.class)
public interface RestaurantInternalFeignClient {

    @PostMapping("/internal/restaurants/{id}/ratings")
    void addRating(@PathVariable("id") Long restaurantId, @RequestBody RatingBody body);

    record RatingBody(int score) {
    }
}
