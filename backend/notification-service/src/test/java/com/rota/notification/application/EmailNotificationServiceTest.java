package com.rota.notification.application;

import com.rota.common.events.OrderStatusChanged;
import com.rota.notification.application.port.EmailSender;
import com.rota.notification.application.port.SentNotifications;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmailNotificationServiceTest {

    private final EmailSender sender = mock(EmailSender.class);
    private final SentNotifications sent = mock(SentNotifications.class);
    private final EmailNotificationService service =
            new EmailNotificationService(new OrderEmailComposer(), sender, sent);

    @Test
    void pagamentoAprovadoEnviaConfirmacaoComTotal() {
        OrderStatusChanged event = event("PAID", "cliente@rota.dev");
        when(sent.reserve(event.eventId())).thenReturn(true);

        service.onOrderStatusChanged(event);

        ArgumentCaptor<EmailMessage> captor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(sender).send(captor.capture());
        assertThat(captor.getValue().to()).isEqualTo("cliente@rota.dev");
        assertThat(captor.getValue().subject()).isEqualTo("Pedido #42 confirmado");
        assertThat(captor.getValue().body()).contains("Burger House").contains("71,79");
    }

    @Test
    void statusIntermediarioNaoGeraEmail() {
        service.onOrderStatusChanged(event("PREPARING", "cliente@rota.dev"));

        verify(sender, never()).send(any());
        verify(sent, never()).reserve(any());
    }

    @Test
    void pedidoSemEmailNaoGeraEmail() {
        service.onOrderStatusChanged(event("DELIVERED", null));

        verify(sender, never()).send(any());
    }

    @Test
    void eventoRepetidoNaoReenviaEmail() {
        OrderStatusChanged event = event("DELIVERED", "cliente@rota.dev");
        when(sent.reserve(event.eventId())).thenReturn(false);

        service.onOrderStatusChanged(event);

        verify(sender, never()).send(any());
    }

    @Test
    void falhaNoEnvioLiberaAReservaParaNovaTentativa() {
        OrderStatusChanged event = event("CANCELLED", "cliente@rota.dev");
        when(sent.reserve(event.eventId())).thenReturn(true);
        doThrow(new IllegalStateException("SMTP fora")).when(sender).send(any());

        assertThatThrownBy(() -> service.onOrderStatusChanged(event)).isInstanceOf(IllegalStateException.class);

        verify(sent).release(event.eventId());
    }

    private static OrderStatusChanged event(String status, String email) {
        return new OrderStatusChanged(UUID.randomUUID(), 42L, 1L, email, 1L, "Burger House", 2L, null,
                "PAYMENT_PENDING", status, new BigDecimal("71.79"), null, Instant.now());
    }
}
