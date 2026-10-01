package com.rota.order.infrastructure.client;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.domain.PaymentMethod;
import feign.FeignException;
import feign.Request;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.springboot3.circuitbreaker.autoconfigure.CircuitBreakerAutoConfiguration;
import io.github.resilience4j.springboot3.retry.autoconfigure.RetryAutoConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.ImportAutoConfiguration;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Sobe só o necessário para as anotações do Resilience4j funcionarem (AOP + registros),
 * com a mesma configuração do application.yml.
 */
@SpringBootTest(classes = PaymentGatewayResilienceTest.Config.class, properties = {
        "resilience4j.circuitbreaker.instances.payment.sliding-window-size=4",
        "resilience4j.circuitbreaker.instances.payment.minimum-number-of-calls=4",
        "resilience4j.circuitbreaker.instances.payment.failure-rate-threshold=50",
        "resilience4j.circuitbreaker.instances.payment.wait-duration-in-open-state=1m",
        "resilience4j.circuitbreaker.instances.payment.record-exceptions="
                + "com.rota.common.exception.ServiceUnavailableException",
        "resilience4j.retry.instances.payment.max-attempts=2",
        "resilience4j.retry.instances.payment.wait-duration=1ms",
        "resilience4j.retry.instances.payment.retry-exceptions="
                + "com.rota.common.exception.ServiceUnavailableException",
})
class PaymentGatewayResilienceTest {

    @ImportAutoConfiguration({AopAutoConfiguration.class, CircuitBreakerAutoConfiguration.class,
            RetryAutoConfiguration.class})
    @Import(PaymentGatewayAdapter.class)
    static class Config {
    }

    @Autowired
    PaymentGateway gateway;

    @Autowired
    CircuitBreakerRegistry circuitBreakers;

    @MockitoBean
    PaymentFeignClient client;

    @BeforeEach
    void reiniciaCircuito() {
        circuitBreakers.circuitBreaker(PaymentGatewayAdapter.RESILIENCE_NAME).reset();
        reset(client);
    }

    @Test
    void falhaDeRedeEhRepetidaAntesDeDesistir() {
        when(client.charge(any())).thenThrow(serviceDown());

        assertThatThrownBy(this::charge).isInstanceOf(ServiceUnavailableException.class);

        verify(client, times(2)).charge(any());
    }

    @Test
    void recusaDeNegocioNaoEhRepetidaNemContaComoFalha() {
        when(client.charge(any())).thenThrow(new BusinessException("Método de pagamento inválido"));

        assertThatThrownBy(this::charge).isInstanceOf(BusinessException.class);

        verify(client, times(1)).charge(any());
        assertThat(circuitBreaker().getMetrics().getNumberOfFailedCalls()).isZero();
    }

    @Test
    void circuitoAbreAposFalhasSeguidasERespondeSemChamarOServico() {
        when(client.charge(any())).thenThrow(serviceDown());
        for (int i = 0; i < 2; i++) {
            assertThatThrownBy(this::charge).isInstanceOf(ServiceUnavailableException.class);
        }
        assertThat(circuitBreaker().getState()).isEqualTo(CircuitBreaker.State.OPEN);
        reset(client);

        assertThatThrownBy(this::charge)
                .isInstanceOf(ServiceUnavailableException.class)
                .hasMessageContaining("temporariamente indisponíveis");
        verify(client, times(0)).charge(any());
    }

    @Test
    void cobrancaAprovadaPassaNormalmente() {
        when(client.charge(any())).thenReturn(new PaymentFeignClient.PaymentBody(1L, "APPROVED", null));

        assertThat(charge().approved()).isTrue();
    }

    private PaymentGateway.PaymentOutcome charge() {
        return gateway.charge(1L, 2L, new BigDecimal("50.00"), PaymentMethod.PIX);
    }

    private CircuitBreaker circuitBreaker() {
        return circuitBreakers.circuitBreaker(PaymentGatewayAdapter.RESILIENCE_NAME);
    }

    private static FeignException serviceDown() {
        Request request = Request.create(Request.HttpMethod.POST, "/internal/payments", Map.of(), null, null, null);
        return new FeignException.ServiceUnavailable("indisponível", request, null, Map.of());
    }
}
