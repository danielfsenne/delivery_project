package com.rota.payment.application.provider;

import com.rota.payment.domain.PaymentMethod;
import com.rota.payment.domain.PaymentResult;
import org.springframework.stereotype.Component;

/**
 * Dinheiro na entrega: não há cobrança online, o pagamento é confirmado
 * para que o pedido siga para o restaurante.
 */
@Component
public class CashOnDeliveryProvider implements PaymentProvider {

    @Override
    public String name() {
        return "cash-on-delivery";
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.CASH;
    }

    @Override
    public PaymentResult process(ChargeRequest request) {
        return PaymentResult.approved("cash_order_" + request.orderId());
    }
}
