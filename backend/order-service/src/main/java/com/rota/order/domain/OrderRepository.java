package com.rota.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByCustomerIdOrderByCreatedAtDesc(Long customerId, Pageable pageable);

    Page<Order> findByRestaurantIdAndRestaurantOwnerIdAndStatusInOrderByCreatedAtDesc(
            Long restaurantId, Long ownerId, Collection<OrderStatus> statuses, Pageable pageable);

    @Query("""
            select count(o) as orders, coalesce(sum(o.total), 0) as revenue
            from Order o
            where o.restaurantId = :restaurantId and o.restaurantOwnerId = :ownerId
              and o.createdAt >= :since and o.status in :statuses
            """)
    SalesTotals salesSince(Long restaurantId, Long ownerId, Instant since, Collection<OrderStatus> statuses);

    long countByRestaurantIdAndRestaurantOwnerIdAndCreatedAtGreaterThanEqualAndStatus(
            Long restaurantId, Long ownerId, Instant since, OrderStatus status);

    long countByRestaurantIdAndRestaurantOwnerIdAndStatusIn(Long restaurantId, Long ownerId,
                                                           Collection<OrderStatus> statuses);

    interface SalesTotals {
        long getOrders();

        BigDecimal getRevenue();
    }
}
