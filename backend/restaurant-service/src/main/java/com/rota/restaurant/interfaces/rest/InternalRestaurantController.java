package com.rota.restaurant.interfaces.rest;

import com.rota.restaurant.application.RatingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints chamados apenas por outros serviços (perfil SERVICE).
 */
@RestController
@RequestMapping("/internal/restaurants")
public class InternalRestaurantController {

    private final RatingService ratingService;

    public InternalRestaurantController(RatingService ratingService) {
        this.ratingService = ratingService;
    }

    @PostMapping("/{id}/ratings")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addRating(@PathVariable Long id, @Valid @RequestBody RatingRequest request) {
        ratingService.addRating(id, request.score());
    }

    public record RatingRequest(@Min(1) @Max(5) int score) {
    }
}
