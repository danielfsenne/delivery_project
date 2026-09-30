package com.rota.delivery.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    Optional<Delivery> findByOrderId(Long orderId);

    List<Delivery> findByStatus(DeliveryStatus status);

    Optional<Delivery> findFirstByDriverIdAndStatusIn(Long driverId, Collection<DeliveryStatus> statuses);

    List<Delivery> findTop50ByDriverIdAndStatusOrderByDeliveredAtDesc(Long driverId, DeliveryStatus status);

    @Query("select coalesce(sum(d.driverFee), 0) from Delivery d where d.driver.id = :driverId and d.status = 'DELIVERED'")
    BigDecimal totalEarnings(Long driverId);

    long countByDriverIdAndStatus(Long driverId, DeliveryStatus status);
}
