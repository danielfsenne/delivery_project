package com.rota.delivery.infrastructure.client;

import com.rota.common.feign.InternalFeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "order-service", url = "${rota.clients.order-url}", configuration = InternalFeignConfig.class)
public interface OrderFeignClient {

    @PostMapping("/internal/orders/{id}/driver")
    void assignDriver(@PathVariable("id") Long orderId, @RequestBody DriverBody body);

    @PostMapping("/internal/orders/{id}/status")
    void changeStatus(@PathVariable("id") Long orderId, @RequestBody StatusBody body);

    record DriverBody(Long driverId) {
    }

    record StatusBody(String status, Long actorId) {
    }
}
