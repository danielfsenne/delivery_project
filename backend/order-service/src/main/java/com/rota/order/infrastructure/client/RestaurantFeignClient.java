package com.rota.order.infrastructure.client;

import com.rota.order.application.port.RestaurantCatalog.Quote;
import com.rota.order.application.port.RestaurantCatalog.QuoteLine;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(name = "restaurant-service", url = "${rota.clients.restaurant-url}",
        configuration = FeignConfig.class)
public interface RestaurantFeignClient {

    @PostMapping("/restaurants/{id}/quote")
    Quote quote(@PathVariable("id") Long restaurantId, @RequestBody QuoteBody body);

    record QuoteBody(List<QuoteLine> items) {
    }
}
