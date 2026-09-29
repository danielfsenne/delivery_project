package com.rota.order.infrastructure.client;

import com.rota.order.application.port.RestaurantCatalog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantCatalogAdapter implements RestaurantCatalog {

    private final RestaurantFeignClient client;
    private final RestaurantInternalFeignClient internalClient;

    public RestaurantCatalogAdapter(RestaurantFeignClient client, RestaurantInternalFeignClient internalClient) {
        this.client = client;
        this.internalClient = internalClient;
    }

    @Override
    public Quote quote(Long restaurantId, List<QuoteLine> lines) {
        return client.quote(restaurantId, new RestaurantFeignClient.QuoteBody(lines));
    }

    @Override
    public void addRating(Long restaurantId, int score) {
        internalClient.addRating(restaurantId, new RestaurantInternalFeignClient.RatingBody(score));
    }
}
