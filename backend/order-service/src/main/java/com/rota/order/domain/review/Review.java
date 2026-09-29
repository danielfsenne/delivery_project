package com.rota.order.domain.review;

import com.rota.common.exception.BusinessException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

/**
 * Avaliação feita pelo cliente após a entrega: nota da comida (restaurante)
 * e, opcionalmente, nota da entrega (entregador).
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false, unique = true)
    private Long orderId;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "food_rating", nullable = false)
    private int foodRating;

    @Column(name = "delivery_rating")
    private Integer deliveryRating;

    private String comment;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Review() {
    }

    public Review(Long orderId, Long customerId, Long restaurantId, Long driverId, int foodRating,
                  Integer deliveryRating, String comment, Instant createdAt) {
        checkScore(foodRating);
        if (deliveryRating != null) {
            checkScore(deliveryRating);
        }
        this.orderId = orderId;
        this.customerId = customerId;
        this.restaurantId = restaurantId;
        this.driverId = driverId;
        this.foodRating = foodRating;
        this.deliveryRating = driverId == null ? null : deliveryRating;
        this.comment = comment == null || comment.isBlank() ? null : comment.trim();
        this.createdAt = createdAt;
    }

    private static void checkScore(int score) {
        if (score < 1 || score > 5) {
            throw new BusinessException("As notas devem ser de 1 a 5");
        }
    }

    public Long getId() {
        return id;
    }

    public Long getOrderId() {
        return orderId;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public int getFoodRating() {
        return foodRating;
    }

    public Integer getDeliveryRating() {
        return deliveryRating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
