package com.muvix.domain.model;

import java.math.BigDecimal;

/**
 * Publicación: un Producto o un Lote. Mapea a la tabla items_vendibles (columna tipo_item).
 *
 * Visibilidad derivada del precio:
 *   precio == 0  -> PRIVADA (existe en el inventario del vendedor, no se puede comprar ni ver en el catálogo)
 *   precio  > 0  -> PÚBLICA (se puede comprar / ofertar)
 * Pasar de privada a pública es simplemente asignarle un precio.
 *
 * El precio de un Lote es independiente del de sus productos: no hay relación obligada.
 */
public abstract sealed class ItemVendible permits Producto, Lote {

    public enum Tipo { PRODUCTO, LOTE }

    public enum Estado { DISPONIBLE, RESERVADO, VENDIDO }

    protected final Long id;
    protected final Long vendedorId;   // seller_id
    protected String titulo;
    protected String descripcion;
    protected BigDecimal precio;       // NOT NULL, 0 = privada
    protected Estado estado;

    protected ItemVendible(Long id, Long vendedorId, String titulo, String descripcion,
                           BigDecimal precio, Estado estado) {
        if (vendedorId == null) throw new DominioException("El vendedor es obligatorio");
        if (titulo == null || titulo.isBlank()) throw new DominioException("El título es obligatorio");
        this.id = id;
        this.vendedorId = vendedorId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = normalizarPrecio(precio);
        this.estado = estado == null ? Estado.DISPONIBLE : estado;
    }

    /** null o 0 => privada. Negativo => error. */
    protected static BigDecimal normalizarPrecio(BigDecimal precio) {
        if (precio == null) return BigDecimal.ZERO;
        if (precio.signum() < 0) throw new DominioException("El precio no puede ser negativo");
        return precio;
    }

    public abstract Tipo tipo();

    // ---- visibilidad / precio ----

    public boolean esPublica() { return precio.signum() > 0; }

    /** Privada -> pública (o cambia el precio de una ya pública). */
    public void publicar(BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.signum() <= 0)
            throw new DominioException("Para publicar hace falta un precio mayor a 0");
        this.precio = nuevoPrecio;
    }

    /** Pública -> privada. */
    public void hacerPrivada() { this.precio = BigDecimal.ZERO; }

    // ---- disponibilidad ----

    public boolean estaDisponible() { return estado == Estado.DISPONIBLE; }

    /** Solo se compra / oferta lo que es público y está disponible. */
    public boolean puedeComprarse() { return esPublica() && estaDisponible(); }

    public void reservar() {
        if (!estaDisponible()) throw new DominioException("El ítem ya no está disponible");
        estado = Estado.RESERVADO;
    }

    public void marcarVendido() {
        if (estado == Estado.VENDIDO) throw new DominioException("El ítem ya fue vendido");
        estado = Estado.VENDIDO;
    }

    public void liberar() {
        if (estado == Estado.VENDIDO) throw new DominioException("Un ítem vendido no se puede liberar");
        estado = Estado.DISPONIBLE;
    }

    public Long getId() { return id; }
    public Long getVendedorId() { return vendedorId; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Estado getEstado() { return estado; }
}