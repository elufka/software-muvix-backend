package com.muvix.domain.model.catalogue;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un producto puede estar:
 *  - suelto y público         (price > 0, en ningún lote)
 *  - solo dentro de lote(s)   (price 0 = privado)
 *  - público Y dentro de lote(s)
 * La pertenencia a lotes vive en Batch (composicion_lotes); el producto no conoce sus lotes.
 */
public final class Product extends SellableItem {

    private String category;
    private final List<String> images = new ArrayList<>();

    public Product(Long id, Long vendedorId, String titulo, String descripcion,
                   BigDecimal price, State state, String categoria, List<String> imagenes) {
        super(id, vendedorId, titulo, descripcion, price, state);
        this.category = categoria;
        if (imagenes != null) this.images.addAll(imagenes);
    }

    /** price null o 0 => producto privado (típico al crearlo "solo para un lote"). */
    public static Product create(Long vendedorId, String titulo, String descripcion,
                                BigDecimal precio, String categoria, List<String> imagenes) {
        return new Product(null, vendedorId, titulo, descripcion, precio,
                State.AVAILABLE, categoria, imagenes);
    }

    @Override public Type type() { return Type.PRODUCT; }

    public String getCategory() { return category; }
    public List<String> getImages() { return List.copyOf(images); }
}