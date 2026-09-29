package com.rota.payment.application.provider;

import com.rota.payment.domain.PaymentMethod;
import com.rota.payment.domain.PaymentResult;

import java.math.BigDecimal;

/**
 * Estratégia de processamento de pagamento. O serviço depende apenas desta abstração;
 * trocar de provedor (fake, Mercado Pago, Stripe) não altera as regras de negócio.
 */
public interface PaymentProvider {

    String name();

    boolean supports(PaymentMethod method);

    PaymentResult process(ChargeRequest request);

    record ChargeRequest(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method) {
    }
}
