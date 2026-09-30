package com.rota.order.application;

import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ServiceUnavailableException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.order.application.port.DeliveryGateway;
import com.rota.order.application.port.PaymentGateway;
import com.rota.order.application.port.PaymentGateway.PaymentOutcome;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PaymentOrchestratorTest {

    private final OrderRepository orders = mock(OrderRepository.class);
    private final PaymentGateway gateway = mock(PaymentGateway.class);
    private final OrderService orderService = new OrderService(orders, new OrderAccessPolicy(),
            mock(DeliveryGateway.class), Clock.systemUTC());
    private final PaymentOrchestrator orchestrator = new PaymentOrchestrator(orderService, gateway);
    private final AuthenticatedUser customer = new AuthenticatedUser(1L, "c@rota.dev", Role.CUSTOMER);

    private Order order;

    @BeforeEach
    void setUp() {
        order = Order.builder()
                .customer(1L)
                .restaurant(10L, "Burger House", 2L)
                .paymentMethod(PaymentMethod.CREDIT_CARD)
                .item(new OrderItem(1L, "X-Bacon", null, null, new BigDecimal("30.00"), 1))
                .build();
        ReflectionTestUtils.setField(order, "id", 50L);
        order.transitionTo(OrderStatus.PAYMENT_PENDING, null, null, Instant.now());
        when(orders.findById(50L)).thenReturn(Optional.of(order));
        when(orders.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void approvedPaymentMovesOrderToPaid() {
        when(gateway.charge(50L, 1L, new BigDecimal("30.00"), PaymentMethod.CREDIT_CARD))
                .thenReturn(new PaymentOutcome(true, null));

        OrderResponse response = orchestrator.pay(customer, 50L);

        assertThat(response.status()).isEqualTo(OrderStatus.PAID);
    }

    @Test
    void declinedPaymentCancelsOrderWithReason() {
        when(gateway.charge(any(), any(), any(), any())).thenReturn(new PaymentOutcome(false, "limite insuficiente"));

        OrderResponse response = orchestrator.pay(customer, 50L);

        assertThat(response.status()).isEqualTo(OrderStatus.CANCELLED);
        assertThat(response.history().getLast().reason()).contains("limite insuficiente");
    }

    @Test
    void unavailablePaymentKeepsOrderPendingAfterCheckout() {
        when(gateway.charge(any(), any(), any(), any()))
                .thenThrow(new ServiceUnavailableException("fora do ar", null));
        OrderResponse created = OrderResponse.from(order);

        OrderResponse response = orchestrator.tryPayAfterCheckout(customer, created);

        assertThat(response.status()).isEqualTo(OrderStatus.PAYMENT_PENDING);
    }

    @Test
    void cannotPayOrderThatIsNotPending() {
        order.transitionTo(OrderStatus.CANCELLED, 1L, null, Instant.now());

        assertThatThrownBy(() -> orchestrator.pay(customer, 50L)).isInstanceOf(ConflictException.class);
        verifyNoInteractions(gateway);
    }
}
