package com.rota.restaurant.application;

import com.rota.common.exception.BusinessException;
import com.rota.common.exception.NotFoundException;
import com.rota.restaurant.domain.Address;
import com.rota.restaurant.domain.Category;
import com.rota.restaurant.domain.OpeningHour;
import com.rota.restaurant.domain.Product;
import com.rota.restaurant.domain.ProductOption;
import com.rota.restaurant.domain.Restaurant;
import com.rota.restaurant.domain.RestaurantDetails;
import com.rota.restaurant.domain.RestaurantRepository;
import com.rota.restaurant.interfaces.rest.dto.QuoteRequest;
import com.rota.restaurant.interfaces.rest.dto.QuoteResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class QuoteServiceTest {

    private final RestaurantRepository repository = mock(RestaurantRepository.class);
    // 2026-01-05 15:00 UTC = 12:00 em São Paulo, segunda-feira
    private final BusinessClock clock = new BusinessClock(
            Clock.fixed(Instant.parse("2026-01-05T15:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo"));
    private final QuoteService service = new QuoteService(repository, clock);

    private Restaurant restaurant;
    private Product burger;
    private ProductOption bacon;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(9L, new RestaurantDetails("Burger House", null, "Hambúrguer", null, null,
                new BigDecimal("5.99"), new BigDecimal("20.00"), 30, 45,
                new Address("Rua A", "1", null, "Centro", "Franca", "SP", "14400-000", null, null),
                Arrays.stream(DayOfWeek.values())
                        .map(d -> new OpeningHour(d, LocalTime.of(11, 0), LocalTime.of(23, 0)))
                        .toList()));
        setId(restaurant, 1L);
        Category category = restaurant.addCategory("Lanches");
        setId(category, 10L);
        burger = category.addProduct("X-Bacon", null, new BigDecimal("32.90"), null);
        setId(burger, 100L);
        bacon = burger.addOption("Bacon extra", new BigDecimal("5.00"));
        setId(bacon, 1000L);
        when(repository.findById(1L)).thenReturn(Optional.of(restaurant));
    }

    @Test
    void shouldPriceItemWithOptionsFromCatalog() {
        QuoteResponse quote = service.quote(1L, new QuoteRequest(List.of(
                new QuoteRequest.Item(100L, 2, List.of(1000L)))));

        assertThat(quote.ownerId()).isEqualTo(9L);
        assertThat(quote.open()).isTrue();
        assertThat(quote.items()).singleElement().satisfies(item -> {
            assertThat(item.unitPrice()).isEqualByComparingTo("37.90");
            assertThat(item.quantity()).isEqualTo(2);
            assertThat(item.options()).extracting(QuoteResponse.Option::name).containsExactly("Bacon extra");
        });
    }

    @Test
    void shouldIgnoreDuplicatedOptionIds() {
        QuoteResponse quote = service.quote(1L, new QuoteRequest(List.of(
                new QuoteRequest.Item(100L, 1, List.of(1000L, 1000L)))));

        assertThat(quote.items().getFirst().unitPrice()).isEqualByComparingTo("37.90");
    }

    @Test
    void shouldRejectUnavailableProduct() {
        burger.setAvailable(false);

        assertThatThrownBy(() -> service.quote(1L, new QuoteRequest(List.of(
                new QuoteRequest.Item(100L, 1, null)))))
                .isInstanceOf(BusinessException.class);
    }

    @Test
    void shouldRejectOptionFromAnotherProduct() {
        assertThatThrownBy(() -> service.quote(1L, new QuoteRequest(List.of(
                new QuoteRequest.Item(100L, 1, List.of(999L))))))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void shouldReportClosedRestaurant() {
        BusinessClock night = new BusinessClock(
                Clock.fixed(Instant.parse("2026-01-05T05:00:00Z"), ZoneOffset.UTC), ZoneId.of("America/Sao_Paulo"));

        QuoteResponse quote = new QuoteService(repository, night).quote(1L, new QuoteRequest(List.of(
                new QuoteRequest.Item(100L, 1, null))));

        assertThat(quote.open()).isFalse();
    }

    private static void setId(Object entity, Long id) {
        ReflectionTestUtils.setField(entity, "id", id);
    }
}
