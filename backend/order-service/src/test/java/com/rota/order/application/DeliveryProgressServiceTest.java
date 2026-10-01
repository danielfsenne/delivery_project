package com.rota.order.application;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.messaging.ProcessedEvents;
import com.rota.order.domain.OrderStatus;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class DeliveryProgressServiceTest {

    private final OrderService orderService = mock(OrderService.class);
    private final ProcessedEvents processedEvents = mock(ProcessedEvents.class);
    private final DeliveryProgressService progress = new DeliveryProgressService(orderService, processedEvents);

    @Test
    void entregadorAceitouAtribuiAoPedido() {
        DeliveryStatusChanged event = event("ASSIGNED");
        when(processedEvents.firstDelivery(event.eventId(), DeliveryProgressService.CONSUMER)).thenReturn(true);

        progress.apply(event);

        verify(orderService).assignDriver(101L, 3L);
    }

    @Test
    void retiradaEEntregaAvancamOPedido() {
        when(processedEvents.firstDelivery(any(), eq(DeliveryProgressService.CONSUMER))).thenReturn(true);

        progress.apply(event("PICKED_UP"));
        progress.apply(event("DELIVERED"));

        verify(orderService).advanceByDelivery(101L, OrderStatus.OUT_FOR_DELIVERY, 3L);
        verify(orderService).advanceByDelivery(101L, OrderStatus.DELIVERED, 3L);
    }

    @Test
    void eventoRepetidoEhIgnorado() {
        when(processedEvents.firstDelivery(any(), any())).thenReturn(false);

        progress.apply(event("DELIVERED"));

        verifyNoInteractions(orderService);
    }

    @Test
    void corridaAbertaNaoMudaOPedido() {
        when(processedEvents.firstDelivery(any(), any())).thenReturn(true);

        progress.apply(event("WAITING_DRIVER"));

        verifyNoInteractions(orderService);
    }

    private static DeliveryStatusChanged event(String status) {
        return new DeliveryStatusChanged(UUID.randomUUID(), 7L, 101L, 1L, 3L, status, Instant.now());
    }
}
