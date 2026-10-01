package com.rota.order.application;

import com.rota.common.events.IntegrationEvent;
import com.rota.common.events.OrderReadyForPickup;
import com.rota.common.events.OrderStatusChanged;
import com.rota.common.events.RotaEvents;
import com.rota.common.messaging.EventPublisher;
import com.rota.order.domain.DeliveryAddress;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.OrderStatusChangedEvent;
import com.rota.order.domain.PaymentMethod;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class OrderIntegrationEventsTest {

    private final EventPublisher publisher = mock(EventPublisher.class);
    private final OrderIntegrationEvents translator = new OrderIntegrationEvents(publisher);
    private final Instant now = Instant.parse("2026-10-01T12:00:00Z");

    private final Order order = Order.builder()
            .customer(1L)
            .customerEmail("cliente@rota.dev")
            .restaurant(10L, "Burger House", 2L)
            .paymentMethod(PaymentMethod.PIX)
            .deliveryAddress(new DeliveryAddress("Rua A", "10", null, "Centro", "Franca", "SP", "14400-000",
                    -20.53, -47.40))
            .pickup(new DeliveryAddress("Rua B", "20", null, "Centro", "Franca", "SP", "14400-000", -20.54, -47.41))
            .deliveryFee(new BigDecimal("5.99"))
            .item(new OrderItem(1L, "X-Bacon", null, null, new BigDecimal("30.00"), 1))
            .build();

    @Test
    void mudancaDeStatusViraEventoComDadosParaNotificacao() {
        translator.on(new OrderStatusChangedEvent(order, OrderStatus.PAID, OrderStatus.RESTAURANT_ACCEPTED,
                null, now));

        ArgumentCaptor<IntegrationEvent> captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(eq(RotaEvents.ORDER_STATUS_CHANGED), captor.capture());
        OrderStatusChanged event = (OrderStatusChanged) captor.getValue();
        assertThat(event.previousStatus()).isEqualTo("PAID");
        assertThat(event.status()).isEqualTo("RESTAURANT_ACCEPTED");
        assertThat(event.customerEmail()).isEqualTo("cliente@rota.dev");
        assertThat(event.restaurantOwnerId()).isEqualTo(2L);
        assertThat(event.total()).isEqualByComparingTo("35.99");
        verify(publisher, never()).publish(eq(RotaEvents.ORDER_READY_FOR_PICKUP), any());
    }

    @Test
    void pedidoProntoTambemAbreACorrida() {
        translator.on(new OrderStatusChangedEvent(order, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP,
                null, now));

        ArgumentCaptor<IntegrationEvent> captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(eq(RotaEvents.ORDER_READY_FOR_PICKUP), captor.capture());
        OrderReadyForPickup ready = (OrderReadyForPickup) captor.getValue();
        assertThat(ready.pickupLatitude()).isEqualTo(-20.54);
        assertThat(ready.dropoffAddress()).contains("Rua A");
        assertThat(ready.deliveryFee()).isEqualByComparingTo("5.99");
    }

    @Test
    void criacaoNaoTemStatusAnterior() {
        translator.on(new OrderStatusChangedEvent(order, null, OrderStatus.CREATED, null, now));

        ArgumentCaptor<IntegrationEvent> captor = ArgumentCaptor.forClass(IntegrationEvent.class);
        verify(publisher).publish(eq(RotaEvents.ORDER_STATUS_CHANGED), captor.capture());
        assertThat(((OrderStatusChanged) captor.getValue()).previousStatus()).isNull();
    }
}
