package com.rota.order.application;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.messaging.ProcessedEvents;
import com.rota.order.domain.OrderStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mantém o pedido em sincronia com a corrida a partir dos eventos do delivery-service.
 * Reentregas do mesmo evento são descartadas; o registro do evento e o efeito no pedido
 * acontecem na mesma transação.
 */
@Service
public class DeliveryProgressService {

    static final String CONSUMER = "order.delivery-progress";

    private final OrderService orderService;
    private final ProcessedEvents processedEvents;

    public DeliveryProgressService(OrderService orderService, ProcessedEvents processedEvents) {
        this.orderService = orderService;
        this.processedEvents = processedEvents;
    }

    @Transactional
    public void apply(DeliveryStatusChanged event) {
        if (!processedEvents.firstDelivery(event.eventId(), CONSUMER)) {
            return;
        }
        switch (event.status()) {
            case "ASSIGNED" -> orderService.assignDriver(event.orderId(), event.driverId());
            case "PICKED_UP" ->
                    orderService.advanceByDelivery(event.orderId(), OrderStatus.OUT_FOR_DELIVERY, event.driverId());
            case "DELIVERED" ->
                    orderService.advanceByDelivery(event.orderId(), OrderStatus.DELIVERED, event.driverId());
            default -> {
                // WAITING_DRIVER: a corrida foi aberta, o pedido não muda.
            }
        }
    }
}
