package com.muvix.domain.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public final class Producto extends ItemVendible {

    private String categoria;
    private final List<String> imagenes = new ArrayList<>();

    public Producto(Long id, Long eventoId, Long vendedorId, String titulo, String descripcion,
                    BigDecimal precio, Estado estado, String categoria, List<String> imagenes) {
        super(id, eventoId, vendedorId, titulo, descripcion, precio, estado);
        this.categoria = categoria;
        if (imagenes != null) this.imagenes.addAll(imagenes);
    }

    /** Factory para "Cargar producto". Un producto suelto siempre necesita precio. */
    public static Producto crear(Long eventoId, Long vendedorId, String titulo, String descripcion,
                                 BigDecimal precio, String categoria, List<String> imagenes) {
        if (precio == null || precio.signum() <= 0) throw new DominioException("El producto necesita un precio");
        return new Producto(null, eventoId, vendedorId, titulo, descripcion, precio,
                Estado.DISPONIBLE, categoria, imagenes);
    }

    @Override public Tipo tipo() { return Tipo.PRODUCTO; }

    public String getCategoria() { return categoria; }
    public List<String> getImagenes() { return List.copyOf(imagenes); }
}
