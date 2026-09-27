package com.rota.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByRestaurantIdAndRestaurantOwnerIdAndStatusInOrderByCreatedAtDesc(
            Long restaurantId, Long ownerId, Collection<OrderStatus> statuses, Pageable pageable);
}
