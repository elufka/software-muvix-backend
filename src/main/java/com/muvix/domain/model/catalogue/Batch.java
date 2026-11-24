package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public final class Batch extends SellableItem {

    private final Set<Long> productIds = new HashSet<>();

    public Batch(Long id, Long sellerId, String title, String description, BigDecimal price,
                 State state, String category, Set<Long> productIds) {
        super(id, sellerId, title, description, price, state,category);
        if (category == null || category.isBlank()) {
            throw new DomainException("Category is required");
        }
        this.category = category.trim();
        if (productIds != null) this.productIds.addAll(productIds);
    }

    public static Batch create(Long sellerId, String title, String description,
                               Collection<Product> products, BigDecimal price, String category) {
        if (products == null || products.size() < 2) {
            throw new DomainException("A batch requires at least 2 products");
        }

        Set<Long> ids = new HashSet<>();
        for (Product p : products) {
            if (p.getId() == null) {
                throw new DomainException("Products must be saved prior to assembling a batch");
            }
            if (!p.getSellerId().equals(sellerId)) {
                throw new DomainException("All products must belong to the same seller");
            }
            if (!p.canBePurchased()) {
                throw new DomainException("Product '" + p.getTitle() + "' must be public and available");
            }
            ids.add(p.getId());
        }
        return new Batch(null, sellerId, title, description, price, State.AVAILABLE, category, ids);
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
        productIds.remove(productId);
        if (productIds.size() < 2) {
            softDelete();
        }
    }

    public void markProductsUnavailable() { softDelete(); }

    public void replaceProducts(Set<Long> replacementIds) {
        if (replacementIds == null || replacementIds.size() < 2) throw new DomainException("A batch requires at least 2 products");
        if (getState() != State.AVAILABLE) throw new DomainException("Only an available batch can be edited");
        productIds.clear();
        productIds.addAll(replacementIds);
    }

    public void deleteBatch() {
        productIds.clear();
        softDelete();
    }

    public void restoreProductAfterRefund(Product product) {
        if (!product.getSellerId().equals(getSellerId())) throw new DomainException("Product belongs to a different seller");
        if (productIds.contains(product.getId())) return;
        if (getState() == State.SOFT_DELETED && productIds.isEmpty()) return;
        if (getState() == State.SOFT_DELETED && productIds.size() >= 1) {
            addProduct(product);
            state = State.AVAILABLE;
        } else if (getState() == State.AVAILABLE) {
            addProduct(product);
        }
    }

    public boolean contains(Long productId) { return productIds.contains(productId); }

    @Override
    public Type type() { return Type.BATCH; }

    public Set<Long> getProductIds() { return Set.copyOf(productIds); }

    public boolean canBePurchased() {
        return isPublic() && isAvailable();
    }
}
