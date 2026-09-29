package com.rota.payment.application;

import com.rota.common.exception.BusinessException;
import com.rota.payment.application.provider.CashOnDeliveryProvider;
import com.rota.payment.application.provider.FakePaymentProvider;
import com.rota.payment.application.provider.PaymentProvider;
import com.rota.payment.application.provider.PaymentProvider.ChargeRequest;
import com.rota.payment.application.provider.PaymentProviderResolver;
import com.rota.payment.domain.Payment;
import com.rota.payment.domain.PaymentMethod;
import com.rota.payment.domain.PaymentRepository;
import com.rota.payment.domain.PaymentResult;
import com.rota.payment.domain.PaymentStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentServiceTest {

    private final PaymentRepository repository = mock(PaymentRepository.class);
    private final PaymentProviderResolver resolver = new PaymentProviderResolver(List.of(
            new FakePaymentProvider(new BigDecimal("500.00")), new CashOnDeliveryProvider()));
    private final PaymentService service = new PaymentService(repository, resolver, Clock.systemUTC());

    @BeforeEach
    void setUp() {
        when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private static ChargeRequest charge(String amount, PaymentMethod method) {
        return new ChargeRequest(10L, 1L, new BigDecimal(amount), method);
    }

    @Test
    void shouldApprovePix() {
        Payment payment = service.charge(charge("80.00", PaymentMethod.PIX));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.APPROVED);
        assertThat(payment.getProvider()).isEqualTo("fake");
        assertThat(payment.getProviderReference()).startsWith("fake_");
    }

    @Test
    void shouldDeclineCardAboveLimit() {
        Payment payment = service.charge(charge("500.01", PaymentMethod.CREDIT_CARD));

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.DECLINED);
        assertThat(payment.getFailureReason()).contains("limite");
    }

    @Test
    void shouldUseCashStrategyForCash() {
        Payment payment = service.charge(charge("900.00", PaymentMethod.CASH));

        assertThat(payment.isApproved()).isTrue();
        assertThat(payment.getProvider()).isEqualTo("cash-on-delivery");
    }

    @Test
    void shouldNotChargeTwiceWhenAlreadyApproved() {
        Payment existing = new Payment(10L, 1L, BigDecimal.TEN, PaymentMethod.PIX, "fake",
                PaymentResult.approved("ref"), Instant.now());
        when(repository.findByOrderIdAndStatus(10L, PaymentStatus.APPROVED)).thenReturn(Optional.of(existing));

        assertThat(service.charge(charge("10.00", PaymentMethod.PIX))).isSameAs(existing);
        verify(repository, never()).save(any());
    }

    @Test
    void shouldFailWhenNoProviderSupportsMethod() {
        PaymentProviderResolver onlyCash = new PaymentProviderResolver(List.<PaymentProvider>of(new CashOnDeliveryProvider()));

        assertThatThrownBy(() -> onlyCash.resolve(PaymentMethod.PIX)).isInstanceOf(BusinessException.class);
    }
}
