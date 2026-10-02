package com.rota.order.application;

import com.rota.common.events.ReviewCreated;
import com.rota.common.events.RotaEvents;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.messaging.EventPublisher;
import com.rota.common.security.AuthenticatedUser;
import com.rota.common.security.Role;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import com.rota.order.domain.review.ReviewRepository;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewRequest;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReviewServiceTest {

    private final ReviewRepository reviews = mock(ReviewRepository.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final EventPublisher events = mock(EventPublisher.class);
    private final ReviewService service = new ReviewService(reviews, orders, events, Clock.systemUTC());

    private final AuthenticatedUser customer = new AuthenticatedUser(1L, "c@rota.dev", Role.CUSTOMER);
    private final ReviewRequest request = new ReviewRequest(5, 4, "Excelente!");
    private Order order;

    @BeforeEach
    void setUp() {
        when(reviews.save(any())).thenAnswer(inv -> inv.getArgument(0));
        order = Order.builder()
                .customer(1L)
                .restaurant(10L, "Burger House", 2L)
                .paymentMethod(PaymentMethod.PIX)
                .item(new OrderItem(1L, "X", null, null, BigDecimal.TEN, 1))
                .build();
        order.assignDriver(3L);
        when(orders.findById(50L)).thenReturn(Optional.of(order));
    }

    private void deliver() {
        for (OrderStatus s : new OrderStatus[]{OrderStatus.PAYMENT_PENDING, OrderStatus.PAID,
                OrderStatus.RESTAURANT_ACCEPTED, OrderStatus.PREPARING, OrderStatus.READY_FOR_PICKUP,
                OrderStatus.OUT_FOR_DELIVERY, OrderStatus.DELIVERED}) {
            order.transitionTo(s, null, null, Instant.now());
        }
    }

    @Test
    void shouldReviewDeliveredOrderAndPublishEvent() {
        deliver();

        ReviewResponse review = service.create(customer, 50L, request);

        assertThat(review.foodRating()).isEqualTo(5);
        assertThat(review.deliveryRating()).isEqualTo(4);
        ArgumentCaptor<ReviewCreated> captor = ArgumentCaptor.forClass(ReviewCreated.class);
        verify(events).publish(eq(RotaEvents.REVIEW_CREATED), captor.capture());
        assertThat(captor.getValue().restaurantId()).isEqualTo(10L);
        assertThat(captor.getValue().foodRating()).isEqualTo(5);
        assertThat(captor.getValue().driverId()).isEqualTo(3L);
    }

    @Test
    void shouldOnlyReviewDeliveredOrders() {
        assertThatThrownBy(() -> service.create(customer, 50L, request)).isInstanceOf(BusinessException.class);
        verify(events, never()).publish(any(), any());
    }

    @Test
    void shouldNotReviewTwice() {
        deliver();
        when(reviews.existsByOrderId(50L)).thenReturn(true);

        assertThatThrownBy(() -> service.create(customer, 50L, request)).isInstanceOf(ConflictException.class);
    }

    @Test
    void shouldNotReviewOthersOrders() {
        deliver();
        AuthenticatedUser other = new AuthenticatedUser(99L, "x@rota.dev", Role.CUSTOMER);

        assertThatThrownBy(() -> service.create(other, 50L, request)).isInstanceOf(ForbiddenException.class);
    }
}
