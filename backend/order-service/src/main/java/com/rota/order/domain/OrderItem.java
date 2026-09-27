package com.rota.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

/**
 * Snapshot do produto no momento da compra: alterações futuras no cardápio
 * não afetam pedidos já feitos.
 */
@Entity
@Table(name = "order_items")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    private String name;

    /** Complementos escolhidos, já formatados (ex.: "Bacon extra, Cheddar extra"). */
    private String options;

    private String notes;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice;

    private int quantity;

    protected OrderItem() {
    }

    public OrderItem(Long productId, String name, String options, String notes, BigDecimal unitPrice, int quantity) {
        this.productId = productId;
        this.name = name;
        this.options = options;
        this.notes = notes;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
    }

    void attachTo(Order order) {
        this.order = order;
    }

    public BigDecimal totalPrice() {
        return unitPrice.multiply(BigDecimal.valueOf(quantity));
    }

    public Long getId() {
        return id;
    }

    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }

    public String getOptions() {
        return options;
    }

    public String getNotes() {
        return notes;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }
}
