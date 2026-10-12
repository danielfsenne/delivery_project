package com.rota.restaurant.domain;

import com.rota.common.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RestaurantCategoriesTest {

    private Restaurant restaurant;
    private Category drinks;
    private Category burgers;
    private Category desserts;

    @BeforeEach
    void setUp() {
        restaurant = new Restaurant(9L, new RestaurantDetails("Burger House", null, "Hambúrguer", null, null,
                new BigDecimal("5.99"), new BigDecimal("20.00"), 30, 45,
                new Address("Rua A", "1", null, "Centro", "Franca", "SP", "14400-000", null, null), List.of()));
        drinks = add("Bebidas", 1L);
        burgers = add("Lanches", 2L);
        desserts = add("Sobremesas", 3L);
    }

    private Category add(String name, Long id) {
        Category category = restaurant.addCategory(name);
        ReflectionTestUtils.setField(category, "id", id);
        return category;
    }

    @Test
    void shouldApplyNewOrderToPositionsAndList() {
        restaurant.reorderCategories(List.of(3L, 1L, 2L));

        assertThat(restaurant.getCategories()).containsExactly(desserts, drinks, burgers);
        assertThat(restaurant.getCategories()).extracting(Category::getPosition).containsExactly(0, 1, 2);
    }

    @Test
    void shouldRejectOrderMissingOrRepeatingCategories() {
        assertThatThrownBy(() -> restaurant.reorderCategories(List.of(1L, 2L)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> restaurant.reorderCategories(List.of(1L, 2L, 2L)))
                .isInstanceOf(BusinessException.class);
        assertThatThrownBy(() -> restaurant.reorderCategories(List.of(1L, 2L, 99L)))
                .isInstanceOf(BusinessException.class);
        assertThat(restaurant.getCategories()).extracting(Category::getPosition).containsExactly(0, 1, 2);
    }

    @Test
    void shouldPlaceNewCategoryAfterTheLastEvenAfterRemovals() {
        restaurant.removeCategory(1L);

        Category snacks = restaurant.addCategory("Porções");

        assertThat(snacks.getPosition()).isEqualTo(3);
        assertThat(burgers.getPosition()).isEqualTo(1);
        assertThat(desserts.getPosition()).isEqualTo(2);
    }
}
