package com.rota.order.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.ConflictException;
import com.rota.common.exception.ForbiddenException;
import com.rota.common.exception.NotFoundException;
import com.rota.common.security.AuthenticatedUser;
import com.rota.order.application.port.RestaurantCatalog;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.review.Review;
import com.rota.order.domain.review.ReviewRepository;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewRequest;
import com.rota.order.interfaces.rest.dto.ReviewDtos.ReviewResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;

@Service
public class ReviewService {

    private static final Logger log = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviews;
    private final OrderRepository orders;
    private final RestaurantCatalog catalog;
    private final TransactionTemplate transaction;
    private final Clock clock;

    public ReviewService(ReviewRepository reviews, OrderRepository orders, RestaurantCatalog catalog,
                         TransactionTemplate transaction, Clock clock) {
        this.reviews = reviews;
        this.orders = orders;
        this.catalog = catalog;
        this.transaction = transaction;
        this.clock = clock;
    }

    /**
     * Grava a avaliação e depois atualiza a média do restaurante. A chamada ao outro serviço
     * fica fora da transação; se falhar, a avaliação continua válida e o erro é registrado.
     */
    public ReviewResponse create(AuthenticatedUser user, Long orderId, ReviewRequest request) {
        Review review = transaction.execute(status -> {
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
            return reviews.save(new Review(orderId, user.id(), order.getRestaurantId(), order.getDriverId(),
                    request.foodRating(), request.deliveryRating(), request.comment(), clock.instant()));
        });

        try {
            catalog.addRating(review.getRestaurantId(), review.getFoodRating());
        } catch (RuntimeException e) {
            log.warn("Avaliação do pedido {} gravada, mas a média do restaurante não foi atualizada: {}",
                    orderId, e.getMessage());
        }
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
