package com.rota.delivery.infrastructure.client;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.delivery.application.port.OrderGateway;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class OrderGatewayAdapter implements OrderGateway {

    private final OrderFeignClient client;

    public OrderGatewayAdapter(OrderFeignClient client) {
        this.client = client;
    }

    @Override
    public void assignDriver(Long orderId, Long driverId) {
        call(() -> client.assignDriver(orderId, new OrderFeignClient.DriverBody(driverId)));
    }

    @Override
    public void markOutForDelivery(Long orderId, Long driverId) {
        call(() -> client.changeStatus(orderId, new OrderFeignClient.StatusBody("OUT_FOR_DELIVERY", driverId)));
    }

    @Override
    public void markDelivered(Long orderId, Long driverId) {
        call(() -> client.changeStatus(orderId, new OrderFeignClient.StatusBody("DELIVERED", driverId)));
    }

    private static void call(Runnable request) {
        try {
            request.run();
        } catch (BusinessException | ConflictException | NotFoundException e) {
            throw e;
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Serviço de pedidos indisponível. Tente novamente.", e);
        }
    }
}
