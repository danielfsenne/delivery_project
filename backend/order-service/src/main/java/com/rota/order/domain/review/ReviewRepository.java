package com.rota.order.domain.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    boolean existsByOrderId(Long orderId);

    Optional<Review> findByOrderId(Long orderId);

    Page<Review> findByRestaurantIdOrderByCreatedAtDesc(Long restaurantId, Pageable pageable);

    @Query("select avg(r.deliveryRating) from Review r where r.driverId = :driverId and r.deliveryRating is not null")
    Double averageDeliveryRating(Long driverId);
}
