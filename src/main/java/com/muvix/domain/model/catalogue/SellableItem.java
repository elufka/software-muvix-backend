package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;
import jakarta.persistence.Version;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public abstract sealed class SellableItem permits Product, Batch {

    public enum Type { PRODUCT, BATCH }
    public enum State { AVAILABLE, RESERVED, SOLD }

    protected final Long id;
    protected final Long sellerId;
    protected String title;
    protected String description;
    protected BigDecimal price; // Price 0 = Private item
    protected State state;
    @Version
    private Long version; // ante problemas de OptimisticLockingFailureException o dos compradores reservando el mismo ítem a la vez

    protected SellableItem(Long id, Long sellerId, String title, String description, BigDecimal price, State state) {
        if (sellerId == null) throw new DomainException("Seller ID is required");
        if (title == null || title.isBlank()) throw new DomainException("Title is required");

        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.price = normalizePrice(price);
        this.state = state == null ? State.AVAILABLE : state;
    }

    protected static BigDecimal normalizePrice(BigDecimal price) {
        if (price == null) return BigDecimal.ZERO;
        if (price.signum() < 0) throw new DomainException("Price cannot be negative");
        return price;
    }

    public abstract Type type();

    public boolean isPublic() { return price.signum() > 0; }

    public void publish(BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() <= 0) {
            throw new DomainException("Publishing requires a price greater than zero");
        }
        this.price = newPrice;
    }

    public void makePrivate() { this.price = BigDecimal.ZERO; }

    public boolean isAvailable() { return state == State.AVAILABLE; }
    public boolean canBePurchased() { return isPublic() && isAvailable(); }

    public void reserve() {
        if (!isAvailable()) throw new DomainException("Item is no longer available");
        this.state = State.RESERVED;
    }

    public void markAsSold() {
        if (state == State.SOLD) throw new DomainException("Item is already sold");
        this.state = State.SOLD;
    }

    public void release() {
        if (state == State.SOLD) throw new DomainException("A sold item cannot be released");
        this.state = State.AVAILABLE;
    }

    // Reconstruccion de sellableItem a Product usada
    public static Product reconstitute(Long id, Long sellerId, String title, String description,
                                       BigDecimal price, State state,
                                       String category, List<String> images) {
        return new Product(id, sellerId, title, description, price, state, category, images);
    }

    // Reconstruccion de sellableItem a Batch
    public static Batch reconstitute(Long id, Long sellerId, String title, String description,
                                     BigDecimal price, State state, Set<Long> products) {
        return new Batch(id, sellerId, title, description, price, state, products);
    }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public State getState() { return state; }
    public Long getVersion() { return version; }
}
