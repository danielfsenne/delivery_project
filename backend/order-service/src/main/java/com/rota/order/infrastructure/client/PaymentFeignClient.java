package com.rota.order.infrastructure.client;

import com.rota.order.domain.PaymentMethod;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "payment-service", url = "${rota.clients.payment-url}", configuration = InternalFeignConfig.class)
public interface PaymentFeignClient {

    @PostMapping("/internal/payments")
    PaymentBody charge(@RequestBody ChargeBody body);

    record ChargeBody(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method) {
    }

    record PaymentBody(Long id, String status, String failureReason) {
    }
}
