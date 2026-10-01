package com.rota.restaurant.application;

import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.restaurant.application.cache.CatalogCaches;
import com.rota.restaurant.application.cache.EvictsRestaurantCache;
import com.rota.restaurant.domain.Category;
import com.rota.restaurant.domain.Product;
import com.rota.restaurant.domain.Restaurant;
import com.rota.restaurant.domain.RestaurantRepository;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.CategoryRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.OptionRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.ProductRequest;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantRequest;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Operações do dono do restaurante (ou de um administrador) sobre o catálogo.
 */
@Service
@Transactional
public class RestaurantManagementService {

    private final RestaurantRepository restaurants;
    private final BusinessClock clock;

    public RestaurantManagementService(RestaurantRepository restaurants, BusinessClock clock) {
        this.restaurants = restaurants;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<RestaurantSummaryResponse> mine(AuthenticatedUser user) {
        return restaurants.findByOwnerIdOrderByNameAsc(user.id()).stream()
                .map(r -> RestaurantSummaryResponse.from(r, r.isOpenAt(clock.now())))
                .toList();
    }

    @Transactional(readOnly = true)
    public RestaurantDetailResponse get(AuthenticatedUser user, Long restaurantId) {
        return toDetail(owned(user, restaurantId));
    }

    @CacheEvict(cacheNames = CatalogCaches.SEARCH, allEntries = true)
    public RestaurantDetailResponse create(AuthenticatedUser user, RestaurantRequest request) {
        Restaurant restaurant = restaurants.save(new Restaurant(user.id(), request.toDetails()));
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse update(AuthenticatedUser user, Long restaurantId, RestaurantRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.update(request.toDetails());
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse setActive(AuthenticatedUser user, Long restaurantId, boolean active) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.setActive(active);
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse addCategory(AuthenticatedUser user, Long restaurantId, CategoryRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        Category category = restaurant.addCategory(request.name().trim());
        if (request.position() != null) {
            category.moveTo(request.position());
        }
        return saveAndReturn(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse updateCategory(AuthenticatedUser user, Long restaurantId, Long categoryId,
                                                   CategoryRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        Category category = restaurant.category(categoryId);
        category.rename(request.name().trim());
        if (request.position() != null) {
            category.moveTo(request.position());
        }
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public void deleteCategory(AuthenticatedUser user, Long restaurantId, Long categoryId) {
        owned(user, restaurantId).removeCategory(categoryId);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse addProduct(AuthenticatedUser user, Long restaurantId, Long categoryId,
                                               ProductRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.category(categoryId)
                .addProduct(request.name().trim(), request.description(), request.price(), request.imageUrl());
        return saveAndReturn(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse updateProduct(AuthenticatedUser user, Long restaurantId, Long productId,
                                                  ProductRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.product(productId)
                .update(request.name().trim(), request.description(), request.price(), request.imageUrl());
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse setProductAvailability(AuthenticatedUser user, Long restaurantId, Long productId,
                                                           boolean available) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.product(productId).setAvailable(available);
        return toDetail(restaurant);
    }

    @EvictsRestaurantCache
    public void deleteProduct(AuthenticatedUser user, Long restaurantId, Long productId) {
        Restaurant restaurant = owned(user, restaurantId);
        Product product = restaurant.product(productId);
        product.getCategory().removeProduct(product);
    }

    @EvictsRestaurantCache
    public RestaurantDetailResponse addOption(AuthenticatedUser user, Long restaurantId, Long productId,
                                              OptionRequest request) {
        Restaurant restaurant = owned(user, restaurantId);
        restaurant.product(productId).addOption(request.name().trim(), request.price());
        return saveAndReturn(restaurant);
    }

    @EvictsRestaurantCache
    public void deleteOption(AuthenticatedUser user, Long restaurantId, Long productId, Long optionId) {
        owned(user, restaurantId).product(productId).removeOption(optionId);
    }

    private Restaurant owned(AuthenticatedUser user, Long restaurantId) {
        Restaurant restaurant = restaurants.findById(restaurantId)
                .orElseThrow(() -> NotFoundException.of("Restaurante", restaurantId));
        if (!user.hasRole(Role.ADMIN) && !restaurant.isOwnedBy(user.id())) {
            throw new ForbiddenException("Você não gerencia este restaurante");
        }
        return restaurant;
    }

    /** Força o flush para que entidades recém-criadas já tenham id na resposta. */
    private RestaurantDetailResponse saveAndReturn(Restaurant restaurant) {
        return toDetail(restaurants.saveAndFlush(restaurant));
    }

    private RestaurantDetailResponse toDetail(Restaurant restaurant) {
        return RestaurantDetailResponse.from(restaurant, restaurant.isOpenAt(clock.now()));
    }
}
