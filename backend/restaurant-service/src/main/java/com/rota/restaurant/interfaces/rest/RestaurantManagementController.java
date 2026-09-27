package com.rota.restaurant.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.restaurant.application.RestaurantManagementService;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.AvailabilityRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.CategoryRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.OptionRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.ProductRequest;
import com.rota.restaurant.interfaces.rest.dto.MenuRequests.StatusRequest;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantRequest;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Gestão dos restaurantes do usuário logado (papel RESTAURANT ou ADMIN).
 */
@RestController
@RequestMapping("/restaurants/mine")
public class RestaurantManagementController {

    private final RestaurantManagementService service;

    public RestaurantManagementController(RestaurantManagementService service) {
        this.service = service;
    }

    @GetMapping
    public List<RestaurantSummaryResponse> list(@AuthenticationPrincipal AuthenticatedUser user) {
        return service.mine(user);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDetailResponse create(@AuthenticationPrincipal AuthenticatedUser user,
                                           @Valid @RequestBody RestaurantRequest request) {
        return service.create(user, request);
    }

    @GetMapping("/{id}")
    public RestaurantDetailResponse get(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id) {
        return service.get(user, id);
    }

    @PutMapping("/{id}")
    public RestaurantDetailResponse update(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                           @Valid @RequestBody RestaurantRequest request) {
        return service.update(user, id, request);
    }

    @PatchMapping("/{id}/status")
    public RestaurantDetailResponse setActive(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                                              @Valid @RequestBody StatusRequest request) {
        return service.setActive(user, id, request.active());
    }

    @PostMapping("/{id}/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDetailResponse addCategory(@AuthenticationPrincipal AuthenticatedUser user,
                                                @PathVariable Long id,
                                                @Valid @RequestBody CategoryRequest request) {
        return service.addCategory(user, id, request);
    }

    @PutMapping("/{id}/categories/{categoryId}")
    public RestaurantDetailResponse updateCategory(@AuthenticationPrincipal AuthenticatedUser user,
                                                   @PathVariable Long id, @PathVariable Long categoryId,
                                                   @Valid @RequestBody CategoryRequest request) {
        return service.updateCategory(user, id, categoryId, request);
    }

    @DeleteMapping("/{id}/categories/{categoryId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@AuthenticationPrincipal AuthenticatedUser user,
                               @PathVariable Long id, @PathVariable Long categoryId) {
        service.deleteCategory(user, id, categoryId);
    }

    @PostMapping("/{id}/categories/{categoryId}/products")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDetailResponse addProduct(@AuthenticationPrincipal AuthenticatedUser user,
                                               @PathVariable Long id, @PathVariable Long categoryId,
                                               @Valid @RequestBody ProductRequest request) {
        return service.addProduct(user, id, categoryId, request);
    }

    @PutMapping("/{id}/products/{productId}")
    public RestaurantDetailResponse updateProduct(@AuthenticationPrincipal AuthenticatedUser user,
                                                  @PathVariable Long id, @PathVariable Long productId,
                                                  @Valid @RequestBody ProductRequest request) {
        return service.updateProduct(user, id, productId, request);
    }

    @PatchMapping("/{id}/products/{productId}/availability")
    public RestaurantDetailResponse setAvailability(@AuthenticationPrincipal AuthenticatedUser user,
                                                    @PathVariable Long id, @PathVariable Long productId,
                                                    @Valid @RequestBody AvailabilityRequest request) {
        return service.setProductAvailability(user, id, productId, request.available());
    }

    @DeleteMapping("/{id}/products/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@AuthenticationPrincipal AuthenticatedUser user,
                              @PathVariable Long id, @PathVariable Long productId) {
        service.deleteProduct(user, id, productId);
    }

    @PostMapping("/{id}/products/{productId}/options")
    @ResponseStatus(HttpStatus.CREATED)
    public RestaurantDetailResponse addOption(@AuthenticationPrincipal AuthenticatedUser user,
                                              @PathVariable Long id, @PathVariable Long productId,
                                              @Valid @RequestBody OptionRequest request) {
        return service.addOption(user, id, productId, request);
    }

    @DeleteMapping("/{id}/products/{productId}/options/{optionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOption(@AuthenticationPrincipal AuthenticatedUser user, @PathVariable Long id,
                             @PathVariable Long productId, @PathVariable Long optionId) {
        service.deleteOption(user, id, productId, optionId);
    }
}
