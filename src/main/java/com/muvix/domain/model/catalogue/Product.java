package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;

import java.math.BigDecimal;
import java.util.List;

public final class Product extends SellableItem {

    private static final int MAX_IMAGES = 10;

    private ProductCondition condition;
    private List<String> images;

    Product(Long id, Long sellerId, String title, String description, BigDecimal price, State state,
            String category, ProductCondition condition, List<String> images) {
        super(id, sellerId, title, description, price, state,category);
        if (category == null || category.isBlank()) {
            throw new DomainException("Category is required");
        }
        if (condition == null) {
            throw new DomainException("Product condition is required");
        }
        this.category = category.trim();
        this.condition = condition;
        this.images = validateAndNormalizeImages(images);
    }

    // Para crear un producto nuevo (sin ID, estado AVAILABLE por defecto)
    public static Product create(Long sellerId, String title, String description, BigDecimal price,
                                 String category, ProductCondition condition, List<String> images) {
        return new Product(null, sellerId, title, description, price, State.AVAILABLE, category, condition, images);
    }

    public static Product createStorageItem(Long sellerId, String title, String description,
                                            String category, ProductCondition condition, List<String> images) {
        return new Product(null, sellerId, title, description, BigDecimal.ZERO, State.PAUSED, category, condition, images);
    }

    // Para reconstruir un producto existente desde la base de datos
    public static Product reconstitute(Long id, Long sellerId, String title, String description, BigDecimal price,
                                       State state, String category, ProductCondition condition, List<String> images) {
        if (id == null) {
            throw new DomainException("Reconstituted product must have an ID");
        }
        return new Product(id, sellerId, title, description, price, state, category, condition, images);
    }

    private List<String> validateAndNormalizeImages(List<String> rawImages) {
        if (rawImages == null || rawImages.isEmpty()) {
            throw new DomainException("A product must have at least one image");
        }

        List<String> cleaned = rawImages.stream()
                .filter(url -> url != null && !url.isBlank())
                .map(String::trim)
                .distinct()
                .toList();

        if (cleaned.isEmpty()) {
            throw new DomainException("A product must have at least one valid image");
        }

        if (cleaned.size() > MAX_IMAGES) {
            throw new DomainException("A product cannot have more than " + MAX_IMAGES + " images");
        }

        return cleaned;
    }

    public void updateDetails(String title, String description, BigDecimal price,
                              String category, ProductCondition condition, List<String> images) {

        boolean wasPrivate = !this.isPublic();

        super.replaceDetails(title, description, price, category);

        if (category == null || category.isBlank()) {
            throw new DomainException("Category is required");
        }
        if (condition == null) {
            throw new DomainException("Product condition is required");
        }
        this.category = category.trim();
        this.condition = condition;
        this.images = validateAndNormalizeImages(images);

        if (wasPrivate && this.price.signum() > 0 && this.state == State.PAUSED) {
            this.state = State.AVAILABLE;
        }
    }


    @Override
    public Type type() {
        return Type.INDIVIDUAL;
    }

    public ProductCondition getCondition() {
        return condition;
    }

    public List<String> getImages() {
        return this.images;
    }
}
