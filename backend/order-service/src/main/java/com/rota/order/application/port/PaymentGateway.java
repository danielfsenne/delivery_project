package com.rota.order.application.port;

import com.rota.order.domain.PaymentMethod;

import java.math.BigDecimal;

/**
 * Porta para o payment-service.
 */
public interface PaymentGateway {

    /**
     * @throws com.rota.common.exception.ServiceUnavailableException se o serviço de pagamento estiver fora do ar
     */
    PaymentOutcome charge(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method);

    record PaymentOutcome(boolean approved, String failureReason) {
    }
}
