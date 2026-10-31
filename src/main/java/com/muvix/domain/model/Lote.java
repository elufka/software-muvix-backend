package com.muvix.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Combo de productos con precio propio (descuento). Referencia a los productos por id
 * (tabla composicion_lotes). Un producto puede estar en N lotes.
 *
 * Reglas:
 *  - un lote contiene SOLO productos (no lotes anidados), todos del mismo evento y vendedor
 *  - mínimo 2 productos
 *
 * La regla "si se vende un producto, los lotes que lo contienen dejan de estar disponibles"
 * involucra a varios aggregates: va en un servicio de dominio / caso de uso, no acá.
 */
public final class Lote extends ItemVendible {

    private final Set<Long> productoIds = new HashSet<>();

    public Lote(Long id, Long eventoId, Long vendedorId, String titulo, String descripcion,
                BigDecimal precio, Estado estado, Set<Long> productoIds) {
        super(id, eventoId, vendedorId, titulo, descripcion, precio, estado);
        if (productoIds != null) this.productoIds.addAll(productoIds);
    }

    /**
     * Factory: arma el lote a partir de productos ya cargados.
     * El precio es el que defina el vendedor; si viene null se calcula como suma menos descuento.
     */
    public static Lote crear(Long eventoId, Long vendedorId, String titulo, String descripcion,
                             Collection<Producto> productos, BigDecimal precio,
                             BigDecimal descuentoSugeridoPct) {
        if (productos == null || productos.size() < 2)
            throw new DominioException("Un lote necesita al menos 2 productos");

        for (Producto p : productos) {
            if (!p.getEventoId().equals(eventoId) || !p.getVendedorId().equals(vendedorId))
                throw new DominioException("Todos los productos deben ser del mismo evento y vendedor");
            if (!p.estaDisponible())
                throw new DominioException("El producto '" + p.getTitulo() + "' no está disponible");
        }

        BigDecimal precioFinal = precio != null ? precio : precioSugerido(productos, descuentoSugeridoPct);
        Set<Long> ids = new HashSet<>();
        productos.forEach(p -> ids.add(p.getId()));
        return new Lote(null, eventoId, vendedorId, titulo, descripcion, precioFinal, Estado.DISPONIBLE, ids);
    }

    public static BigDecimal precioSugerido(Collection<Producto> productos, BigDecimal descuentoPct) {
        BigDecimal suma = productos.stream().map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        if (descuentoPct == null) return suma;
        return suma.multiply(BigDecimal.valueOf(100).subtract(descuentoPct))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public void agregarProducto(Producto p) {
        if (!p.getEventoId().equals(eventoId) || !p.getVendedorId().equals(vendedorId))
            throw new DominioException("El producto no pertenece a este evento/vendedor");
        productoIds.add(p.getId());
    }

    public void quitarProducto(Long productoId) {
        if (productoIds.size() <= 2) throw new DominioException("Un lote necesita al menos 2 productos");
        productoIds.remove(productoId);
    }

    public boolean contiene(Long productoId) { return productoIds.contains(productoId); }

    @Override public Tipo tipo() { return Tipo.LOTE; }

    public Set<Long> getProductoIds() { return Set.copyOf(productoIds); }
}
