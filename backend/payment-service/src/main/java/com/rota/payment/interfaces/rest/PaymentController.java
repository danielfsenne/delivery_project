package com.rota.payment.interfaces.rest;

import com.rota.common.security.AuthenticatedUser;
import com.rota.payment.application.PaymentService;
import com.rota.payment.application.provider.PaymentProvider.ChargeRequest;
import com.rota.payment.domain.Payment;
import com.rota.payment.domain.PaymentMethod;
import com.rota.payment.domain.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@RestController
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /** Chamado pelo order-service com token de serviço. */
    @PostMapping("/internal/payments")
    public PaymentResponse charge(@Valid @RequestBody ChargeBody body) {
        return PaymentResponse.from(paymentService.charge(
                new ChargeRequest(body.orderId(), body.customerId(), body.amount(), body.method())));
    }

    @GetMapping("/payments/order/{orderId}")
    public List<PaymentResponse> forOrder(@AuthenticationPrincipal AuthenticatedUser user,
                                          @PathVariable Long orderId) {
        return paymentService.forOrder(user, orderId).stream().map(PaymentResponse::from).toList();
    }

    public record ChargeBody(@NotNull Long orderId, @NotNull Long customerId,
                             @NotNull @DecimalMin("0.00") BigDecimal amount, @NotNull PaymentMethod method) {
    }

    public record PaymentResponse(Long id, Long orderId, BigDecimal amount, PaymentMethod method,
                                  PaymentStatus status, String provider, String providerReference,
                                  String failureReason, Instant createdAt) {
        static PaymentResponse from(Payment p) {
            return new PaymentResponse(p.getId(), p.getOrderId(), p.getAmount(), p.getMethod(), p.getStatus(),
                    p.getProvider(), p.getProviderReference(), p.getFailureReason(), p.getCreatedAt());
        }
    }
}
