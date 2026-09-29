package com.rota.order.domain.coupon;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {

    Optional<Coupon> findByCode(String code);

    boolean existsByCode(String code);

    List<Coupon> findAllByOrderByCreatedAtDesc();

    /**
     * Incrementa o uso de forma atômica. Retorna 0 se o limite já foi atingido,
     * evitando que dois checkouts simultâneos ultrapassem o limite.
     */
    @Modifying
    @Query("""
            update Coupon c set c.usedCount = c.usedCount + 1
            where c.id = :id and (c.usageLimit is null or c.usedCount < c.usageLimit)
            """)
    int incrementUsage(Long id);
}
