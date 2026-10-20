package com.muvix.domain.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Lot {
    private Long id;
    private String name;
    private List<Publication> publications;
    private BigDecimal total;
    private LocalDateTime createdAt;


    // Constructor vacío
    public Lot() {
    }

    // Constructor con campos
    public Lot(Long id, String name, List<Publication> publications, BigDecimal total, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.publications = publications;
        this.total = total;
        this.createdAt = createdAt;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Publication> getPublications() {
        return publications;
    }

    public void setPublications(List<Publication> publications) {
        this.publications = publications;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Lot{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", publications=" + publications +
                ", total=" + total +
                ", createdAt=" + createdAt +
                '}';
    }
}
