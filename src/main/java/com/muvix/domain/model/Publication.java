package com.muvix.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Publication {
    private Long id;
    private String title;
    private String description;
    private List<Item> items;
    private Integer quantity;
    private PublicationStatus status;
    private BigDecimal price;
    private LocalDateTime createdAt;

    // Constructor vacío
    public Publication() {
    }

    // Constructor con campos
    public Publication(Long id, String title, String description, List<Item> items, Integer quantity, PublicationStatus status, BigDecimal price, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.items = items;
        this.quantity = quantity;
        this.status = status;
        this.price = price;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public PublicationStatus getStatus() {
        return status;
    }

    public void setStatus(PublicationStatus status) {
        this.status = status;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    @Override
    public String toString() {
        return "Publication{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", quantity=" + quantity +
                ", status=" + status +
                ", price=" + price +
                ", createdAt=" + createdAt +
                '}';
    }
}

