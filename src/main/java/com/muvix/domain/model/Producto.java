package com.muvix.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Un producto puede estar:
 *  - suelto y público         (precio > 0, en ningún lote)
 *  - solo dentro de lote(s)   (precio 0 = privado)
 *  - público Y dentro de lote(s)
 * La pertenencia a lotes vive en Lote (composicion_lotes); el producto no conoce sus lotes.
 */
public final class Producto extends ItemVendible {

    private String categoria;
    private final List<String> imagenes = new ArrayList<>();

    public Producto(Long id, Long vendedorId, String titulo, String descripcion,
                    BigDecimal precio, Estado estado, String categoria, List<String> imagenes) {
        super(id, vendedorId, titulo, descripcion, precio, estado);
        this.categoria = categoria;
        if (imagenes != null) this.imagenes.addAll(imagenes);
    }

    /** precio null o 0 => producto privado (típico al crearlo "solo para un lote"). */
    public static Producto crear(Long vendedorId, String titulo, String descripcion,
                                 BigDecimal precio, String categoria, List<String> imagenes) {
        return new Producto(null, vendedorId, titulo, descripcion, precio,
                Estado.DISPONIBLE, categoria, imagenes);
    }

    @Override public Tipo tipo() { return Tipo.PRODUCTO; }

    public String getCategoria() { return categoria; }
    public List<String> getImagenes() { return List.copyOf(imagenes); }
}