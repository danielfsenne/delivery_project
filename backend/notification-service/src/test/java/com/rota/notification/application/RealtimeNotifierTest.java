package com.rota.notification.application;

import com.rota.common.events.DeliveryStatusChanged;
import com.rota.common.events.DriverLocationUpdated;
import com.rota.common.events.OrderStatusChanged;
import com.rota.notification.application.port.RealtimeGateway;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class RealtimeNotifierTest {

    private final RealtimeGateway gateway = mock(RealtimeGateway.class);
    private final RealtimeNotifier notifier = new RealtimeNotifier(gateway);

    @Test
    void mudancaDePedidoAvisaClienteERestaurante() {
        notifier.on(new OrderStatusChanged(UUID.randomUUID(), 42L, 1L, "c@rota.dev", 10L, "Burger House", 2L,
                null, "PAID", "RESTAURANT_ACCEPTED", BigDecimal.TEN, null, Instant.now()));

        ArgumentCaptor<RealtimeMessage> customer = ArgumentCaptor.forClass(RealtimeMessage.class);
        verify(gateway).toUser(eq(1L), customer.capture());
        assertThat(customer.getValue().type()).isEqualTo(RealtimeMessage.ORDER_STATUS);
        assertThat(((RealtimeMessage.OrderUpdate) customer.getValue().payload()).status())
                .isEqualTo("RESTAURANT_ACCEPTED");

        ArgumentCaptor<RealtimeMessage> owner = ArgumentCaptor.forClass(RealtimeMessage.class);
        verify(gateway).toUser(eq(2L), owner.capture());
        assertThat(owner.getValue().type()).isEqualTo(RealtimeMessage.RESTAURANT_ORDER);
    }

    @Test
    void mudancaDeCorridaVaiParaOsEntregadores() {
        notifier.on(new DeliveryStatusChanged(UUID.randomUUID(), 7L, 42L, 1L, null, "WAITING_DRIVER",
                Instant.now()));

        verify(gateway).toDrivers(any());
        verify(gateway, never()).toUser(any(), any());
    }

    @Test
    void posicaoDoEntregadorVaiSoParaOClienteDoPedido() {
        notifier.on(new DriverLocationUpdated(UUID.randomUUID(), 3L, 42L, 1L, -20.53, -47.40, Instant.now()));

        ArgumentCaptor<RealtimeMessage> captor = ArgumentCaptor.forClass(RealtimeMessage.class);
        verify(gateway).toUser(eq(1L), captor.capture());
        RealtimeMessage.LocationUpdate location = (RealtimeMessage.LocationUpdate) captor.getValue().payload();
        assertThat(location.orderId()).isEqualTo(42L);
        assertThat(location.latitude()).isEqualTo(-20.53);
    }
}
