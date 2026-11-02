package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Batch extends SellableItem {

    private final Set<Long> productIds = new HashSet<>();

    public Batch(Long id, Long sellerId, String title, String description, BigDecimal price, State state, Set<Long> productIds) {
        super(id, sellerId, title, description, price, state);
        if (productIds != null) this.productIds.addAll(productIds);
    }

    public static Batch create(Long sellerId, String title, String description, Collection<Product> products, BigDecimal price) {
        if (products == null || products.size() < 2) {
            throw new DomainException("A batch requires at least 2 products");
        }

        Set<Long> ids = new HashSet<>();
        for (Product p : products) {
            if (p.getId() == null) throw new DomainException("Products must be saved prior to assembling a batch");
            if (!p.getSellerId().equals(sellerId)) {
                throw new DomainException("All products must belong to the same seller");
            }
            if (!p.isAvailable()) {
                throw new DomainException("Product '" + p.getTitle() + "' is not available");
            }
            ids.add(p.getId());
        }
        return new Batch(null, sellerId, title, description, price, State.AVAILABLE, ids);
    }

    public static BigDecimal sumIndividualPrices(Collection<Product> products) {
        return products.stream()
                .filter(Product::isPublic)
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addProduct(Product p) {
        if (!p.getSellerId().equals(sellerId)) {
            throw new DomainException("Product does not belong to the same seller");
        }
        if (p.getId() == null) throw new DomainException("Product must be saved");
        productIds.add(p.getId());
    }

    public void removeProduct(Long productId) {
        if (!productIds.contains(productId)) return;
        if (productIds.size() <= 2) throw new DomainException("A batch must contain at least 2 products");
        productIds.remove(productId);
    }

    public boolean contains(Long productId) { return productIds.contains(productId); }

    @Override
    public Type type() { return Type.BATCH; }

    public Set<Long> getProductIds() { return Set.copyOf(productIds); }
}
