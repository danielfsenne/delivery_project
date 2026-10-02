package com.rota.notification.application;

import com.rota.common.events.OrderStatusChanged;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.Optional;

/**
 * Decide quais mudanças de status viram e-mail e monta o texto. Só os marcos que o cliente
 * quer saber mesmo com o app fechado; o resto chega pela notificação em tempo real.
 */
public class OrderEmailComposer {

    private static final Locale PT_BR = Locale.of("pt", "BR");

    public Optional<EmailMessage> compose(OrderStatusChanged event) {
        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            return Optional.empty();
        }
        String order = "Pedido #" + event.orderId();
        String restaurant = event.restaurantName();

        return Optional.ofNullable(switch (event.status()) {
            case "PAID" -> new EmailMessage(event.customerEmail(), order + " confirmado",
                    """
                    Recebemos o pagamento do seu pedido no %s.
                    Total: %s

                    Avisaremos quando ele sair para entrega.
                    """.formatted(restaurant, money(event.total())));
            case "OUT_FOR_DELIVERY" -> new EmailMessage(event.customerEmail(), order + " saiu para entrega",
                    """
                    Seu pedido do %s está a caminho.
                    Acompanhe a entrega em tempo real pelo app.
                    """.formatted(restaurant));
            case "DELIVERED" -> new EmailMessage(event.customerEmail(), order + " entregue",
                    """
                    Seu pedido do %s foi entregue. Bom apetite!
                    Conta pra gente como foi: avalie o pedido no app.
                    """.formatted(restaurant));
            case "CANCELLED" -> new EmailMessage(event.customerEmail(), order + " cancelado",
                    """
                    Seu pedido do %s foi cancelado.%s
                    Se houve cobrança, o valor será estornado.
                    """.formatted(restaurant, event.reason() == null ? "" : "\nMotivo: " + event.reason()));
            default -> null;
        });
    }

    private static String money(BigDecimal value) {
        return value == null ? "-" : NumberFormat.getCurrencyInstance(PT_BR).format(value);
    }
}
