package com.rota.order.application;

import com.rota.common.exception.BusinessException;
import com.rota.order.application.port.RestaurantCatalog;
import com.rota.order.application.port.RestaurantCatalog.Quote;
import com.rota.order.application.port.RestaurantCatalog.QuotedItem;
import com.rota.order.application.port.RestaurantCatalog.QuotedOption;
import com.rota.order.domain.Order;
import com.rota.order.domain.OrderRepository;
import com.rota.order.domain.OrderStatus;
import com.rota.order.domain.PaymentMethod;
import com.rota.order.domain.cart.Cart;
import com.rota.order.domain.cart.CartItem;
import com.rota.order.domain.cart.CartRepository;
import com.rota.order.interfaces.rest.dto.OrderDtos.AddressRequest;
import com.rota.order.interfaces.rest.dto.OrderDtos.CheckoutRequest;
import com.rota.order.interfaces.rest.dto.OrderDtos.OrderResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CheckoutServiceTest {

    private static final Long CUSTOMER = 1L;

    private final CartRepository carts = mock(CartRepository.class);
    private final RestaurantCatalog catalog = mock(RestaurantCatalog.class);
    private final OrderRepository orders = mock(OrderRepository.class);
    private final CouponService coupons = mock(CouponService.class);
    private final CheckoutService service = new CheckoutService(new CartService(carts, catalog, coupons), coupons,
            catalog, orders, Clock.fixed(Instant.parse("2026-01-01T12:00:00Z"), ZoneOffset.UTC));

    private final CheckoutRequest request = new CheckoutRequest(
            new AddressRequest("Rua B", "10", null, "Centro", "Franca", "SP", "14400-000", null, null),
            PaymentMethod.PIX, "Sem cebola");

    @BeforeEach
    void setUp() {
        when(orders.saveAndFlush(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    private void givenCart(BigDecimal minOrder) {
        givenCart(minOrder, null);
    }

    private void givenCart(BigDecimal minOrder, String coupon) {
        CartItem item = new CartItem("i1", 100L, "X-Bacon (antigo)", new BigDecimal("30.00"), 2, List.of(), "bem passado");
        Cart cart = new Cart(CUSTOMER, 10L, "Burger House", new BigDecimal("5.99"), minOrder, List.of(item), coupon);
        when(carts.findByUserId(CUSTOMER)).thenReturn(Optional.of(cart));
    }

    private void givenQuote(boolean open, BigDecimal minOrder) {
        Quote quote = new Quote(10L, "Burger House", 2L, open, new BigDecimal("5.99"), minOrder, null, List.of(
                new QuotedItem(100L, "X-Bacon", new BigDecimal("37.90"), 2,
                        List.of(new QuotedOption(1L, "Bacon extra", new BigDecimal("5.00"))))));
        when(catalog.quote(eq(10L), anyList())).thenReturn(quote);
    }

    @Test
    void shouldCreateOrderWithCurrentPricesAndClearCart() {
        givenCart(BigDecimal.ZERO);
        givenQuote(true, BigDecimal.ZERO);

        OrderResponse order = service.checkout(CUSTOMER, request);

        assertThat(order.status()).isEqualTo(OrderStatus.PAYMENT_PENDING);
        assertThat(order.subtotal()).isEqualByComparingTo("75.80");
        assertThat(order.total()).isEqualByComparingTo("81.79");
        assertThat(order.items()).singleElement().satisfies(i -> {
            assertThat(i.name()).isEqualTo("X-Bacon");
            assertThat(i.options()).isEqualTo("Bacon extra");
            assertThat(i.notes()).isEqualTo("bem passado");
        });
        assertThat(order.history()).extracting(h -> h.newStatus())
                .containsExactly(OrderStatus.CREATED, OrderStatus.PAYMENT_PENDING);
        verify(carts).deleteByUserId(CUSTOMER);
    }

    @Test
    void shouldRedeemCouponAndApplyDiscount() {
        givenCart(BigDecimal.ZERO, "SAVE10");
        givenQuote(true, BigDecimal.ZERO);
        when(coupons.redeem("SAVE10", new BigDecimal("75.80"), 10L)).thenReturn(new BigDecimal("7.58"));

        OrderResponse order = service.checkout(CUSTOMER, request);

        assertThat(order.couponCode()).isEqualTo("SAVE10");
        assertThat(order.discount()).isEqualByComparingTo("7.58");
        assertThat(order.total()).isEqualByComparingTo("74.21");
    }

    @Test
    void shouldNotCreateOrderWhenCouponIsInvalid() {
        givenCart(BigDecimal.ZERO, "VENCIDO");
        givenQuote(true, BigDecimal.ZERO);
        when(coupons.redeem(any(), any(), any())).thenThrow(new BusinessException("Cupom VENCIDO expirou"));

        assertThatThrownBy(() -> service.checkout(CUSTOMER, request)).hasMessageContaining("expirou");
        verify(orders, never()).saveAndFlush(any(Order.class));
        verify(carts, never()).deleteByUserId(any());
    }

    @Test
    void shouldRejectEmptyCart() {
        when(carts.findByUserId(CUSTOMER)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.checkout(CUSTOMER, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("vazio");
    }

    @Test
    void shouldRejectClosedRestaurant() {
        givenCart(BigDecimal.ZERO);
        givenQuote(false, BigDecimal.ZERO);

        assertThatThrownBy(() -> service.checkout(CUSTOMER, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("fechado");
        verify(orders, never()).saveAndFlush(any(Order.class));
        verify(carts, never()).deleteByUserId(any());
    }

    @Test
    void shouldRejectOrderBelowMinimum() {
        givenCart(new BigDecimal("100.00"));
        givenQuote(true, new BigDecimal("100.00"));

        assertThatThrownBy(() -> service.checkout(CUSTOMER, request))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Pedido mínimo");
    }
}
