package com.rota.restaurant.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "categories")
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    private String name;

    private int position;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Product> products = new ArrayList<>();

    protected Category() {
    }

    Category(Restaurant restaurant, String name, int position) {
        this.restaurant = restaurant;
        this.name = name;
        this.position = position;
    }

    public Product addProduct(String name, String description, BigDecimal price, String imageUrl) {
        Product product = new Product(this, name, description, price, imageUrl);
        products.add(product);
        return product;
    }

    public void removeProduct(Product product) {
        products.remove(product);
    }

    public void rename(String name) {
        this.name = name;
    }

    public void moveTo(int position) {
        this.position = position;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getPosition() {
        return position;
    }

    public List<Product> getProducts() {
        return List.copyOf(products);
    }
}
