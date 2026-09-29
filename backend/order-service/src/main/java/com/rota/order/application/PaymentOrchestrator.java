package com.rota.order.application;

import com.rota.common.exception.ServiceUnavailableException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.OrderService.PayableOrder;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.application.port.PaymentGateway.PaymentOutcome;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Cobra o pedido no payment-service e aplica o resultado.
 * A chamada HTTP fica fora de transação de banco: nenhuma conexão fica presa
 * esperando o outro serviço responder.
 */
@Service
public class PaymentOrchestrator {

    private static final Logger log = LoggerFactory.getLogger(PaymentOrchestrator.class);

    private final OrderService orderService;
    private final PaymentGateway paymentGateway;

    public PaymentOrchestrator(OrderService orderService, PaymentGateway paymentGateway) {
        this.orderService = orderService;
        this.paymentGateway = paymentGateway;
    }

    public OrderResponse pay(AuthenticatedUser user, Long orderId) {
        PayableOrder order = orderService.payable(user, orderId);
        PaymentOutcome outcome = paymentGateway.charge(order.orderId(), order.customerId(), order.amount(),
                order.method());
        return orderService.applyPayment(orderId, outcome);
    }

    /**
     * Tentativa automática logo após o checkout. Se o pagamento estiver fora do ar,
     * o pedido continua aguardando pagamento e o cliente pode tentar de novo.
     */
    public OrderResponse tryPayAfterCheckout(AuthenticatedUser user, OrderResponse created) {
        try {
            return pay(user, created.id());
        } catch (ServiceUnavailableException e) {
            log.warn("Pedido {} criado, mas o pagamento está indisponível: {}", created.id(), e.getMessage());
            return created;
        }
    }
}
