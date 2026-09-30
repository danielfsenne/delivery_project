package com.rota.order.infrastructure.client;

import com.rota.common.exception.ServiceUnavailableException;
import com.rota.order.application.port.DeliveryGateway;
import com.rota.order.domain.DeliveryAddress;
import com.rota.order.domain.Order;
import feign.FeignException;
import org.springframework.stereotype.Component;

@Component
public class DeliveryGatewayAdapter implements DeliveryGateway {

    private final DeliveryFeignClient client;

    public DeliveryGatewayAdapter(DeliveryFeignClient client) {
        this.client = client;
    }

    @Override
    public void requestDelivery(Order order) {
        DeliveryAddress dropoff = order.getDeliveryAddress();
        try {
            client.create(new DeliveryFeignClient.DeliveryRequest(order.getId(), order.getCustomerId(),
                    order.getRestaurantId(), order.getRestaurantName(), order.getPickupAddress(),
                    order.getPickupLatitude(), order.getPickupLongitude(), dropoff.formatted(),
                    dropoff.latitude(), dropoff.longitude(), order.getDeliveryFee()));
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Serviço de entregas indisponível. Tente novamente.", e);
        }
    }
}
