package com.rota.order.infrastructure.client;

import com.rota.common.exception.ServiceUnavailableException;
import com.rota.order.application.port.RestaurantCatalog;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RestaurantCatalogAdapter implements RestaurantCatalog {

    static final String RESILIENCE_NAME = "restaurant";

    private final RestaurantFeignClient client;

    public RestaurantCatalogAdapter(RestaurantFeignClient client) {
        this.client = client;
    }

    /**
     * A cotação é só leitura, então pode ser repetida. Erros de negócio (produto inexistente,
     * restaurante fechado) chegam como exceções de domínio e não acionam retry nem circuit breaker.
     */
    @Override
    @Retry(name = RESILIENCE_NAME, fallbackMethod = "circuitOpen")
    @CircuitBreaker(name = RESILIENCE_NAME)
    public Quote quote(Long restaurantId, List<QuoteLine> lines) {
        try {
            return client.quote(restaurantId, new RestaurantFeignClient.QuoteBody(lines));
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Catálogo de restaurantes indisponível. Tente novamente.", e);
        }
    }

    @SuppressWarnings("unused")
    private Quote circuitOpen(Long restaurantId, List<QuoteLine> lines, CallNotPermittedException e) {
        throw new ServiceUnavailableException(
                "Catálogo de restaurantes temporariamente indisponível. Tente em instantes.", e);
    }
}
