package com.muvix.domain.model;

import java.math.BigDecimal;

/**
 * Abstracción de lo que se puede vender: un Producto suelto o un Lote (combo).
 * Mapea 1:1 con la tabla items_vendibles (columna tipo_item).
 * sealed: el dominio conoce todos los subtipos, así podés usar switch exhaustivo.
 */
public abstract sealed class ItemVendible permits Producto, Lote {

    public enum Tipo { PRODUCTO, LOTE }

    public enum Estado { DISPONIBLE, RESERVADO, VENDIDO }

    protected final Long id;
    protected final Long eventoId;
    protected final Long vendedorId;   // seller_id
    protected String titulo;
    protected String descripcion;
    protected BigDecimal precio;
    protected Estado estado;

    protected ItemVendible(Long id, Long eventoId, Long vendedorId, String titulo,
                           String descripcion, BigDecimal precio, Estado estado) {
        if (titulo == null || titulo.isBlank()) throw new DominioException("El título es obligatorio");
        if (precio != null && precio.signum() < 0) throw new DominioException("El precio no puede ser negativo");
        this.id = id;
        this.eventoId = eventoId;
        this.vendedorId = vendedorId;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.estado = estado == null ? Estado.DISPONIBLE : estado;
    }

    public abstract Tipo tipo();

    public boolean estaDisponible() { return estado == Estado.DISPONIBLE; }

    public void reservar() {
        if (!estaDisponible()) throw new DominioException("El ítem ya no está disponible");
        estado = Estado.RESERVADO;
    }

    public void marcarVendido() {
        if (estado == Estado.VENDIDO) throw new DominioException("El ítem ya fue vendido");
        estado = Estado.VENDIDO;
    }

    /** Se usa al cancelar un pedido o rechazar una reserva. */
    public void liberar() {
        if (estado == Estado.VENDIDO) throw new DominioException("Un ítem vendido no se puede liberar");
        estado = Estado.DISPONIBLE;
    }

    public void cambiarPrecio(BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.signum() <= 0) throw new DominioException("Precio inválido");
        this.precio = nuevoPrecio;
    }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public Long getVendedorId() { return vendedorId; }
    public String getTitulo() { return titulo; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Estado getEstado() { return estado; }
}
