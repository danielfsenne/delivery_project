package com.rota.order.application;

import com.rota.common.events.ReviewCreated;
import com.rota.common.events.RotaEvents;
import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.messaging.EventPublisher;
import com.rota.common.security.AuthenticatedUser;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.review.Review;
import com.rota.order.domain.review.ReviewRepository;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewRequest;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviews;
    private final OrderRepository orders;
    private final EventPublisher events;
    private final Clock clock;

    public ReviewService(ReviewRepository reviews, OrderRepository orders, EventPublisher events, Clock clock) {
        this.reviews = reviews;
        this.orders = orders;
        this.events = events;
        this.clock = clock;
    }

    /**
     * Grava a avaliação e publica {@code review.created} na mesma transação. A média do
     * restaurante é atualizada pelo restaurant-service ao consumir o evento, mesmo que ele
     * esteja fora do ar neste momento.
     */
    @Transactional
    public ReviewResponse create(AuthenticatedUser user, Long orderId, ReviewRequest request) {
        Order order = orders.findById(orderId).orElseThrow(() -> NotFoundException.of("Pedido", orderId));
        if (!order.belongsToCustomer(user.id())) {
            throw new ForbiddenException("Você só pode avaliar os seus pedidos");
        }
        if (order.getStatus() != OrderStatus.DELIVERED) {
            throw new BusinessException("Só é possível avaliar pedidos entregues");
        }
        if (reviews.existsByOrderId(orderId)) {
            throw new ConflictException("Este pedido já foi avaliado");
        }
        Review review = reviews.save(new Review(orderId, user.id(), order.getRestaurantId(), order.getDriverId(),
                request.foodRating(), request.deliveryRating(), request.comment(), clock.instant()));

        events.publish(RotaEvents.REVIEW_CREATED, new ReviewCreated(UUID.randomUUID(), review.getId(), orderId,
                review.getRestaurantId(), review.getDriverId(), review.getFoodRating(), review.getDeliveryRating(),
                review.getCreatedAt()));
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public ReviewResponse forOrder(AuthenticatedUser user, Long orderId) {
        Review review = reviews.findByOrderId(orderId)
                .orElseThrow(() -> new NotFoundException("Pedido " + orderId + " ainda não foi avaliado"));
        if (!review.getCustomerId().equals(user.id())) {
            throw new ForbiddenException("Você não tem acesso a esta avaliação");
        }
        return ReviewResponse.from(review);
    }

    @Transactional(readOnly = true)
    public Page<ReviewResponse> forRestaurant(Long restaurantId, Pageable pageable) {
        return reviews.findByRestaurantIdOrderByCreatedAtDesc(restaurantId, pageable).map(ReviewResponse::from);
    }
}
