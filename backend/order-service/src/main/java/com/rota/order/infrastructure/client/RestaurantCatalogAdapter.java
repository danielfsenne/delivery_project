package com.rota.order.infrastructure.client;

import com.rota.order.application.port.RestaurantCatalog;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantCatalogAdapter implements RestaurantCatalog {

    private final RestaurantFeignClient client;

    public RestaurantCatalogAdapter(RestaurantFeignClient client) {
        this.client = client;
    }

    @Override
    public Quote quote(Long restaurantId, List<QuoteLine> lines) {
        return client.quote(restaurantId, new RestaurantFeignClient.QuoteBody(lines));
    }
}
