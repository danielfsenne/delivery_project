package com.rota.order.domain;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collection;
import java.util.List;

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

    // Visão da plataforma inteira, para o administrador.

    Page<Order> findByStatusInOrderByCreatedAtDesc(Collection<OrderStatus> statuses, Pageable pageable);

    @Query("""
            select count(o) as orders, coalesce(sum(o.total), 0) as revenue
            from Order o
            where o.createdAt >= :since and o.status in :statuses
            """)
    SalesTotals platformSalesSince(Instant since, Collection<OrderStatus> statuses);

    long countByCreatedAtGreaterThanEqualAndStatus(Instant since, OrderStatus status);

    long countByStatusIn(Collection<OrderStatus> statuses);

    @Query("""
            select o.restaurantId as restaurantId, o.restaurantName as restaurantName,
                   count(o) as orders, sum(o.total) as revenue
            from Order o
            where o.createdAt >= :since and o.status in :statuses
            group by o.restaurantId, o.restaurantName
            order by sum(o.total) desc
            """)
    List<RestaurantSales> topRestaurantsSince(Instant since, Collection<OrderStatus> statuses, Pageable pageable);

    interface SalesTotals {
        long getOrders();

        BigDecimal getRevenue();
    }

    interface RestaurantSales {
        Long getRestaurantId();

        String getRestaurantName();

        long getOrders();

        BigDecimal getRevenue();
    }
}
