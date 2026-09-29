package com.rota.restaurant.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.restaurant.domain.Product;
import com.rota.restaurant.domain.ProductOption;
import com.rota.restaurant.domain.Restaurant;
import com.rota.restaurant.domain.RestaurantRepository;
import com.rota.restaurant.interfaces.rest.dto.QuoteRequest;
import com.rota.restaurant.interfaces.rest.dto.QuoteResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * Precifica itens com os dados atuais do catálogo. É a fonte da verdade de preços
 * para carrinho e pedido: nomes e valores enviados pelo cliente são ignorados.
 */
@Service
@Transactional(readOnly = true)
public class QuoteService {

    private final RestaurantRepository restaurants;
    private final BusinessClock clock;

    public QuoteService(RestaurantRepository restaurants, BusinessClock clock) {
        this.restaurants = restaurants;
        this.clock = clock;
    }

    public QuoteResponse quote(Long restaurantId, QuoteRequest request) {
        Restaurant restaurant = restaurants.findById(restaurantId)
                .filter(Restaurant::isActive)
                .orElseThrow(() -> NotFoundException.of("Restaurante", restaurantId));

        List<QuoteResponse.Item> items = request.items().stream()
                .map(item -> quoteItem(restaurant, item))
                .toList();

        return new QuoteResponse(restaurant.getId(), restaurant.getName(), restaurant.getOwnerId(),
                restaurant.isOpenAt(clock.now()), restaurant.getDeliveryFee(), restaurant.getMinOrderValue(),
                restaurant.getAddress(), items);
    }

    private QuoteResponse.Item quoteItem(Restaurant restaurant, QuoteRequest.Item item) {
        Product product = restaurant.product(item.productId());
        if (!product.isAvailable()) {
            throw new BusinessException("Produto indisponível: " + product.getName());
        }
        List<QuoteResponse.Option> options = new LinkedHashSet<>(item.optionIds()).stream()
                .map(product::option)
                .map(QuoteService::toOption)
                .toList();
        BigDecimal unitPrice = options.stream()
                .map(QuoteResponse.Option::price)
                .reduce(product.getPrice(), BigDecimal::add);
        return new QuoteResponse.Item(product.getId(), product.getName(), unitPrice, item.quantity(), options);
    }

    private static QuoteResponse.Option toOption(ProductOption option) {
        return new QuoteResponse.Option(option.getId(), option.getName(), option.getPrice());
    }
}
