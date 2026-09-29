package com.rota.order.application;

import com.rota.common.exception.BusinessException;
import com.rota.order.application.port.RestaurantCatalog;
import com.rota.order.application.port.RestaurantCatalog.Quote;
import com.rota.order.application.port.RestaurantCatalog.QuoteLine;
import com.rota.order.application.port.RestaurantCatalog.QuotedItem;
import com.rota.order.application.port.RestaurantCatalog.QuotedOption;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderItem;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.cart.Cart;
import com.rota.order.domain.cart.CartItem;
import com.rota.order.interfaces.rest.dto.OrderDtos.CheckoutRequest;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Transforma o carrinho em pedido. Os itens são cotados novamente no fechamento,
 * então o pedido sempre reflete preços e disponibilidade atuais do cardápio.
 */
@Service
public class CheckoutService {

    private final CartService cartService;
    private final CouponService couponService;
    private final RestaurantCatalog catalog;
    private final OrderRepository orders;
    private final Clock clock;

    public CheckoutService(CartService cartService, CouponService couponService, RestaurantCatalog catalog,
                           OrderRepository orders, Clock clock) {
        this.cartService = cartService;
        this.couponService = couponService;
        this.catalog = catalog;
        this.orders = orders;
        this.clock = clock;
    }

    @Transactional
    public OrderResponse checkout(Long customerId, CheckoutRequest request) {
        Cart cart = cartService.get(customerId);
        if (cart.isEmpty()) {
            throw new BusinessException("O carrinho está vazio");
        }

        List<CartItem> cartItems = cart.items();
        Quote quote = catalog.quote(cart.restaurantId(), cartItems.stream()
                .map(i -> new QuoteLine(i.productId(), i.quantity(), i.optionIds()))
                .toList());

        if (!quote.open()) {
            throw new BusinessException(quote.restaurantName() + " está fechado no momento");
        }

        List<OrderItem> items = new ArrayList<>();
        for (int i = 0; i < cartItems.size(); i++) {
            items.add(toOrderItem(quote.items().get(i), cartItems.get(i).notes()));
        }
        BigDecimal subtotal = items.stream().map(OrderItem::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add);

        if (subtotal.compareTo(quote.minOrderValue()) < 0) {
            throw new BusinessException("Pedido mínimo para " + quote.restaurantName() + " é R$ "
                    + quote.minOrderValue());
        }

        BigDecimal discount = cart.couponCode() == null
                ? BigDecimal.ZERO
                : couponService.redeem(cart.couponCode(), subtotal, quote.restaurantId());

        Order.Builder builder = Order.builder()
                .customer(customerId)
                .restaurant(quote.restaurantId(), quote.restaurantName(), quote.ownerId())
                .paymentMethod(request.paymentMethod())
                .deliveryAddress(request.deliveryAddress().toAddress())
                .pickup(quote.address())
                .deliveryFee(quote.deliveryFee())
                .coupon(cart.couponCode(), discount)
                .notes(request.notes())
                .createdAt(clock.instant());
        items.forEach(builder::item);
        Order order = builder.build();

        order.transitionTo(OrderStatus.PAYMENT_PENDING, null, null, clock.instant());
        OrderResponse response = OrderResponse.from(orders.saveAndFlush(order));
        cartService.clear(customerId);
        return response;
    }

    private static OrderItem toOrderItem(QuotedItem quoted, String notes) {
        String options = quoted.options().isEmpty()
                ? null
                : quoted.options().stream().map(QuotedOption::name).collect(Collectors.joining(", "));
        return new OrderItem(quoted.productId(), quoted.name(), options, notes, quoted.unitPrice(), quoted.quantity());
    }
}
