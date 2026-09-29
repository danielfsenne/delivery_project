package com.rota.order.infrastructure.client;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.domain.PaymentMethod;
import feign.FeignException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class PaymentGatewayAdapter implements PaymentGateway {

    private final PaymentFeignClient client;

    public PaymentGatewayAdapter(PaymentFeignClient client) {
        this.client = client;
    }

    @Override
    public PaymentOutcome charge(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method) {
        try {
            PaymentFeignClient.PaymentBody payment =
                    client.charge(new PaymentFeignClient.ChargeBody(orderId, customerId, amount, method));
            return new PaymentOutcome("APPROVED".equals(payment.status()), payment.failureReason());
        } catch (BusinessException e) {
            throw e;
        } catch (FeignException e) {
            throw new ServiceUnavailableException("Serviço de pagamento indisponível. Tente novamente.", e);
        }
    }
}
