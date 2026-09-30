package com.rota.order.infrastructure.client;

import com.rota.common.feign.InternalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;

@FeignClient(name = "delivery-service", url = "${rota.clients.delivery-url}", configuration = InternalFeignConfig.class)
public interface DeliveryFeignClient {

    @PostMapping("/internal/deliveries")
    void create(@RequestBody DeliveryRequest request);

    record DeliveryRequest(Long orderId, Long customerId, Long restaurantId, String restaurantName,
                           String pickupAddress, Double pickupLatitude, Double pickupLongitude,
                           String dropoffAddress, Double dropoffLatitude, Double dropoffLongitude,
                           BigDecimal deliveryFee) {
    }
}
