package com.rota.restaurant.interfaces.rest;

import com.rota.restaurant.application.CatalogQueryService;
import com.rota.restaurant.application.QuoteService;
import com.rota.restaurant.interfaces.rest.dto.QuoteRequest;
import com.rota.restaurant.interfaces.rest.dto.QuoteResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantDetailResponse;
import com.rota.restaurant.interfaces.rest.dto.RestaurantSummaryResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consulta pública do catálogo.
 */
@RestController
@RequestMapping("/restaurants")
public class CatalogController {

    private final CatalogQueryService catalog;
    private final QuoteService quoteService;

    public CatalogController(CatalogQueryService catalog, QuoteService quoteService) {
        this.catalog = catalog;
        this.quoteService = quoteService;
    }

    @GetMapping
    public Page<RestaurantSummaryResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String cuisine,
            @RequestParam(required = false) String q,
            @PageableDefault(size = 20, sort = "name", direction = Sort.Direction.ASC) Pageable pageable) {
        return catalog.search(city, cuisine, q, pageable);
    }

    @GetMapping("/{id}")
    public RestaurantDetailResponse detail(@PathVariable Long id) {
        return catalog.detail(id);
    }

    /**
     * Usado pelo order-service para precificar carrinho e pedido.
     */
    @PostMapping("/{id}/quote")
    public QuoteResponse quote(@PathVariable Long id, @Valid @RequestBody QuoteRequest request) {
        return quoteService.quote(id, request);
    }
}
