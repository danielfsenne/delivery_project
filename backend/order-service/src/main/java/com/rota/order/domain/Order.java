package com.rota.order.domain;

import com.rota.common.exception.ConflictException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order extends AbstractAggregateRoot<Order> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "restaurant_id", nullable = false)
    private Long restaurantId;

    @Column(name = "restaurant_name", nullable = false)
    private String restaurantName;

    @Column(name = "restaurant_owner_id", nullable = false)
    private Long restaurantOwnerId;

    @Column(name = "driver_id")
    private Long driverId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", nullable = false)
    private PaymentMethod paymentMethod;

    private BigDecimal subtotal;

    @Column(name = "delivery_fee")
    private BigDecimal deliveryFee;

    private BigDecimal discount;

    private BigDecimal total;

    private String notes;

    @Column(name = "coupon_code")
    private String couponCode;

    @Embedded
    private DeliveryAddress deliveryAddress;

    @Column(name = "pickup_address")
    private String pickupAddress;

    @Column(name = "pickup_latitude")
    private Double pickupLatitude;

    @Column(name = "pickup_longitude")
    private Double pickupLongitude;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<OrderItem> items = new ArrayList<>();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("createdAt ASC, id ASC")
    private List<OrderHistory> history = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Version
    private long version;

    protected Order() {
    }

    private Order(Builder b) {
        this.customerId = b.customerId;
        this.customerEmail = b.customerEmail;
        this.restaurantId = b.restaurantId;
        this.restaurantName = b.restaurantName;
        this.restaurantOwnerId = b.restaurantOwnerId;
        this.paymentMethod = b.paymentMethod;
        this.deliveryAddress = b.deliveryAddress;
        if (b.pickup != null) {
            this.pickupAddress = b.pickup.formatted();
            this.pickupLatitude = b.pickup.latitude();
            this.pickupLongitude = b.pickup.longitude();
        }
        this.notes = b.notes;
        this.couponCode = b.couponCode;
        this.deliveryFee = money(b.deliveryFee);
        this.discount = money(b.discount == null ? BigDecimal.ZERO : b.discount);
        this.createdAt = b.now;
        this.updatedAt = b.now;
        b.items.forEach(item -> {
            item.attachTo(this);
            items.add(item);
        });
        this.subtotal = money(items.stream().map(OrderItem::totalPrice).reduce(BigDecimal.ZERO, BigDecimal::add));
        this.total = money(subtotal.add(deliveryFee).subtract(discount).max(BigDecimal.ZERO));
        this.status = OrderStatus.CREATED;
        history.add(new OrderHistory(this, null, OrderStatus.CREATED, customerId, null, b.now));
        registerEvent(new OrderStatusChangedEvent(this, null, OrderStatus.CREATED, null, b.now));
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Único ponto de mudança de status. Transições fora da máquina de estados
     * geram {@link ConflictException} (HTTP 409).
     */
    public void transitionTo(OrderStatus target, Long actorId, String reason, Instant now) {
        if (!status.canTransitionTo(target)) {
            throw new ConflictException("Transição inválida: %s -> %s".formatted(status, target));
        }
        history.add(new OrderHistory(this, status, target, actorId, reason, now));
        OrderStatus previous = status;
        this.status = target;
        this.updatedAt = now;
        registerEvent(new OrderStatusChangedEvent(this, previous, target, reason, now));
    }

    public void assignDriver(Long driverId) {
        this.driverId = driverId;
    }

    public boolean belongsToCustomer(Long userId) {
        return customerId.equals(userId);
    }

    public boolean belongsToRestaurantOwner(Long userId) {
        return restaurantOwnerId.equals(userId);
    }

    public boolean isAssignedToDriver(Long userId) {
        return driverId != null && driverId.equals(userId);
    }

    private static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_EVEN);
    }

    public Long getId() {
        return id;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Long getRestaurantId() {
        return restaurantId;
    }

    public String getRestaurantName() {
        return restaurantName;
    }

    public Long getRestaurantOwnerId() {
        return restaurantOwnerId;
    }

    public Long getDriverId() {
        return driverId;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public BigDecimal getDeliveryFee() {
        return deliveryFee;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public String getNotes() {
        return notes;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public DeliveryAddress getDeliveryAddress() {
        return deliveryAddress;
    }

    public String getPickupAddress() {
        return pickupAddress;
    }

    public Double getPickupLatitude() {
        return pickupLatitude;
    }

    public Double getPickupLongitude() {
        return pickupLongitude;
    }

    public List<OrderItem> getItems() {
        return List.copyOf(items);
    }

    public List<OrderHistory> getHistory() {
        return List.copyOf(history);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public static final class Builder {
        private Long customerId;
        private String customerEmail;
        private Long restaurantId;
        private String restaurantName;
        private Long restaurantOwnerId;
        private PaymentMethod paymentMethod;
        private DeliveryAddress deliveryAddress;
        private DeliveryAddress pickup;
        private String notes;
        private BigDecimal deliveryFee = BigDecimal.ZERO;
        private BigDecimal discount;
        private String couponCode;
        private final List<OrderItem> items = new ArrayList<>();
        private Instant now = Instant.now();

        public Builder customer(Long customerId) {
            this.customerId = customerId;
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder restaurant(Long id, String name, Long ownerId) {
            this.restaurantId = id;
            this.restaurantName = name;
            this.restaurantOwnerId = ownerId;
            return this;
        }

        public Builder paymentMethod(PaymentMethod paymentMethod) {
            this.paymentMethod = paymentMethod;
            return this;
        }

        public Builder deliveryAddress(DeliveryAddress address) {
            this.deliveryAddress = address;
            return this;
        }

        /** Endereço do restaurante, onde o entregador retira o pedido. */
        public Builder pickup(DeliveryAddress pickup) {
            this.pickup = pickup;
            return this;
        }

        public Builder notes(String notes) {
            this.notes = notes;
            return this;
        }

        public Builder deliveryFee(BigDecimal deliveryFee) {
            this.deliveryFee = deliveryFee;
            return this;
        }

        public Builder discount(BigDecimal discount) {
            this.discount = discount;
            return this;
        }

        public Builder coupon(String code, BigDecimal discount) {
            this.couponCode = code;
            this.discount = discount;
            return this;
        }

        public Builder item(OrderItem item) {
            this.items.add(item);
            return this;
        }

        public Builder createdAt(Instant now) {
            this.now = now;
            return this;
        }

        public Order build() {
            if (items.isEmpty()) {
                throw new IllegalStateException("Pedido sem itens");
            }
            return new Order(this);
        }
    }
}
