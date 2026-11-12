package com.muvix.domain.model.catalogue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Product extends SellableItem {

    private String category;
    private final List<String> images = new ArrayList<>();

    public Product(Long id, Long sellerId, String title, String description, BigDecimal price, State state, String category, List<String> images) {
        super(id, sellerId, title, description, price, state);
        this.category = category;
        if (images != null) this.images.addAll(images);
    }

    public static Product create(Long sellerId, String title, String description, BigDecimal price, String category, List<String> images) {
        return new Product(null, sellerId, title, description, price, State.AVAILABLE, category, images);
    }

    @Override
    public Type type() { return Type.PRODUCT; }

    public String getCategory() { return category; }
    public List<String> getImages() { return List.copyOf(images); }
}
