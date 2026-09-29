package com.rota.payment.application.provider;

import com.rota.common.exception.BusinessException;
import com.rota.payment.domain.PaymentMethod;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Escolhe a estratégia que atende a forma de pagamento.
 */
@Component
public class PaymentProviderResolver {

    private final List<PaymentProvider> providers;

    public PaymentProviderResolver(List<PaymentProvider> providers) {
        this.providers = List.copyOf(providers);
    }

    public PaymentProvider resolve(PaymentMethod method) {
        return providers.stream()
                .filter(p -> p.supports(method))
                .findFirst()
                .orElseThrow(() -> new BusinessException("Forma de pagamento indisponível: " + method));
    }
}
