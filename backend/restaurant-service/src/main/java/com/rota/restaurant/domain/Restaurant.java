package com.rota.restaurant.domain;

import com.rota.common.exception.NotFoundException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Raiz do agregado do catálogo: categorias, produtos e complementos são alterados
 * sempre através do restaurante.
 */
@Entity
@Table(name = "restaurants")
public class Restaurant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false)
    private Long ownerId;

    private String name;
    private String description;
    private String cuisine;
    private String phone;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    @Column(name = "min_order_value")
    private BigDecimal minOrderValue;

    @Column(name = "delivery_time_min")
    private int deliveryTimeMin;

    @Column(name = "delivery_time_max")
    private int deliveryTimeMax;

    private boolean active = true;

    @Embedded
    private Address address;

    @ElementCollection
    @CollectionTable(name = "opening_hours", joinColumns = @JoinColumn(name = "restaurant_id"))
    private List<OpeningHour> openingHours = new ArrayList<>();

    @OneToMany(mappedBy = "restaurant", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC, id ASC")
    private List<Category> categories = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Restaurant() {
    }

    public Restaurant(Long ownerId, RestaurantDetails details) {
        this.ownerId = ownerId;
        update(details);
    }

    public void update(RestaurantDetails details) {
        this.name = details.name();
        this.description = details.description();
        this.cuisine = details.cuisine();
        this.phone = details.phone();
        this.imageUrl = details.imageUrl();
        this.deliveryFee = details.deliveryFee();
        this.minOrderValue = details.minOrderValue();
        this.deliveryTimeMin = details.deliveryTimeMin();
        this.deliveryTimeMax = details.deliveryTimeMax();
        this.address = details.address();
        this.openingHours.clear();
        this.openingHours.addAll(details.openingHours());
    }

    public boolean isOwnedBy(Long userId) {
        return ownerId.equals(userId);
    }

    public boolean isOpenAt(LocalDateTime moment) {
        return active && openingHours.stream().anyMatch(h -> h.covers(moment));
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Category addCategory(String name) {
        Category category = new Category(this, name, categories.size());
        categories.add(category);
        return category;
    }

    public Category category(Long categoryId) {
        return categories.stream()
                .filter(c -> c.getId().equals(categoryId))
                .findFirst()
                .orElseThrow(() -> NotFoundException.of("Categoria", categoryId));
    }

    public void removeCategory(Long categoryId) {
        categories.remove(category(categoryId));
    }

    public Product product(Long productId) {
        return categories.stream()
                .flatMap(c -> c.getProducts().stream())
                .filter(p -> p.getId().equals(productId))
                .findFirst()
                .orElseThrow(() -> NotFoundException.of("Produto", productId));
    }

    public Long getId() {
        return id;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getCuisine() {
        return cuisine;
    }

    public String getPhone() {
        return phone;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getMinOrderValue() {
        return minOrderValue;
    }

    public int getDeliveryTimeMin() {
        return deliveryTimeMin;
    }

    public int getDeliveryTimeMax() {
        return deliveryTimeMax;
    }

    public boolean isActive() {
        return active;
    }

    public Address getAddress() {
        return address;
    }

    public List<OpeningHour> getOpeningHours() {
        return List.copyOf(openingHours);
    }

    public List<Category> getCategories() {
        return List.copyOf(categories);
    }
}
