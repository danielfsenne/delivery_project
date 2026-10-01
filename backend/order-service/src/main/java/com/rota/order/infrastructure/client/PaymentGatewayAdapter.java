package com.rota.order.infrastructure.client;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.domain.PaymentMethod;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Retry é seguro aqui: o payment-service devolve o pagamento já aprovado quando o
 * mesmo pedido é cobrado de novo. Só falhas de infraestrutura contam para o circuit breaker;
 * um cartão recusado é resposta válida.
 */
@Component
public class PaymentGatewayAdapter implements PaymentGateway {

    static final String RESILIENCE_NAME = "payment";

    private final PaymentFeignClient client;

    public PaymentGatewayAdapter(PaymentFeignClient client) {
        this.client = client;
    }

    @Override
    @Retry(name = RESILIENCE_NAME, fallbackMethod = "circuitOpen")
    @CircuitBreaker(name = RESILIENCE_NAME)
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

    /** Circuito aberto: responde na hora, sem esperar o timeout de um serviço que já está falhando. */
    @SuppressWarnings("unused")
    private PaymentOutcome circuitOpen(Long orderId, Long customerId, BigDecimal amount, PaymentMethod method,
                                       CallNotPermittedException e) {
        throw new ServiceUnavailableException(
                "Pagamentos temporariamente indisponíveis. Seu pedido foi mantido; tente pagar em instantes.", e);
    }
}
