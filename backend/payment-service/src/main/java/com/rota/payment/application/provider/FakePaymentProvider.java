package com.rota.payment.application.provider;

import com.rota.payment.domain.PaymentMethod;
import com.rota.payment.domain.PaymentResult;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Provedor simulado para desenvolvimento e demonstração.
 * Pix é sempre aprovado; cartão é recusado acima de um limite configurável,
 * permitindo demonstrar o fluxo de pagamento recusado.
 */
@Component
@ConditionalOnProperty(prefix = "rota.payment", name = "provider", havingValue = "fake", matchIfMissing = true)
public class FakePaymentProvider implements PaymentProvider {

    private final BigDecimal cardLimit;

    public FakePaymentProvider(@Value("${rota.payment.fake.card-limit:500.00}") BigDecimal cardLimit) {
        this.cardLimit = cardLimit;
    }

    @Override
    public String name() {
        return "fake";
    }

    @Override
    public boolean supports(PaymentMethod method) {
        return method == PaymentMethod.PIX || method == PaymentMethod.CREDIT_CARD;
    }

    @Override
    public PaymentResult process(ChargeRequest request) {
        if (request.method() == PaymentMethod.CREDIT_CARD && request.amount().compareTo(cardLimit) > 0) {
            return PaymentResult.declined("Cartão recusado: limite insuficiente");
        }
        return PaymentResult.approved("fake_" + UUID.randomUUID());
    }
}
