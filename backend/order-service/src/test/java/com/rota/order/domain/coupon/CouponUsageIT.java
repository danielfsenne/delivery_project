package com.rota.order.domain.coupon;

import com.rota.order.support.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * O limite de uso do cupom depende de um UPDATE condicional no banco. Só um Postgres real
 * mostra que checkouts simultâneos não passam do limite.
 */
class CouponUsageIT extends IntegrationTest {

    @Autowired
    CouponRepository coupons;

    @Autowired
    TransactionTemplate transaction;

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void usosSimultaneosNaoUltrapassamOLimite() throws Exception {
        jdbc.update("""
                INSERT INTO coupons (code, type, value, min_order_value, usage_limit)
                VALUES ('CORRIDA3', 'FIXED', 10, 0, 3)
                """);
        Long id = coupons.findByCode("CORRIDA3").orElseThrow().getId();

        int attempts = 20;
        Callable<Integer> redeem = () -> transaction.execute(status -> coupons.incrementUsage(id));
        List<Future<Integer>> results = new ArrayList<>();
        try (ExecutorService pool = Executors.newFixedThreadPool(attempts)) {
            for (int i = 0; i < attempts; i++) {
                results.add(pool.submit(redeem));
            }
        }

        int granted = 0;
        for (Future<Integer> result : results) {
            granted += result.get();
        }
        assertThat(granted).isEqualTo(3);
        assertThat(jdbc.queryForObject("SELECT used_count FROM coupons WHERE id = ?", Integer.class, id))
                .isEqualTo(3);
    }
}
