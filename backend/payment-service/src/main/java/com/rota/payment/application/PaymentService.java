package com.rota.payment.application;

import com.rota.common.exception.ForbiddenException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.payment.application.provider.PaymentProvider;
import com.rota.payment.application.provider.PaymentProvider.ChargeRequest;
import com.rota.payment.application.provider.PaymentProviderResolver;
import com.rota.payment.domain.Payment;
import com.rota.payment.domain.PaymentRepository;
import com.rota.payment.domain.PaymentResult;
import com.rota.payment.domain.PaymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class PaymentService {

    private static final Logger log = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository payments;
    private final PaymentProviderResolver resolver;
    private final Clock clock;

    public PaymentService(PaymentRepository payments, PaymentProviderResolver resolver, Clock clock) {
        this.payments = payments;
        this.resolver = resolver;
        this.clock = clock;
    }

    /**
     * Idempotente: se o pedido já tem pagamento aprovado, devolve-o sem cobrar de novo.
     */
    @Transactional
    public Payment charge(ChargeRequest request) {
        return payments.findByOrderIdAndStatus(request.orderId(), PaymentStatus.APPROVED)
                .orElseGet(() -> process(request));
    }

    @Transactional(readOnly = true)
    public List<Payment> forOrder(AuthenticatedUser user, Long orderId) {
        List<Payment> attempts = payments.findByOrderIdOrderByCreatedAtDesc(orderId);
        boolean allowed = user.hasRole(Role.ADMIN)
                || attempts.stream().allMatch(p -> p.getCustomerId().equals(user.id()));
        if (!allowed) {
            throw new ForbiddenException("Você não tem acesso a estes pagamentos");
        }
        return attempts;
    }

    private Payment process(ChargeRequest request) {
        PaymentProvider provider = resolver.resolve(request.method());
        PaymentResult result = provider.process(request);
        log.info("Pagamento do pedido {} via {}: {}", request.orderId(), provider.name(),
                result.approved() ? "aprovado" : "recusado (" + result.failureReason() + ")");
        return payments.save(new Payment(request.orderId(), request.customerId(), request.amount(),
                request.method(), provider.name(), result, clock.instant()));
    }
}
