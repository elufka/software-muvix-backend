package com.muvix.domain.model;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Publicación que agrupa productos bajo un precio PROPIO, sin relación con la suma de los
 * precios individuales (puede ser mayor, menor o igual). precio 0 => lote privado.
 *
 * Un producto puede estar en N lotes. Solo contiene productos (no lotes anidados).
 *
 * Cómo se crea (lo orquesta el caso de uso, no el dominio):
 *  - desde productos existentes: se pasan sus ids
 *  - desde cero: el caso de uso crea primero los productos (públicos con precio, o privados
 *    con precio 0/null) y los persiste; luego llama a Lote.crear con los productos ya con id.
 */
public final class Lote extends ItemVendible {

    private final Set<Long> productoIds = new HashSet<>();

    public Lote(Long id, Long vendedorId, String titulo, String descripcion,
                BigDecimal precio, Estado estado, Set<Long> productoIds) {
        super(id, vendedorId, titulo, descripcion, precio, estado);
        if (productoIds != null) this.productoIds.addAll(productoIds);
    }

    public static Lote crear(Long vendedorId, String titulo, String descripcion,
                             Collection<Producto> productos, BigDecimal precio) {
        if (productos == null || productos.size() < 2)
            throw new DominioException("Un lote necesita al menos 2 productos");

        Set<Long> ids = new HashSet<>();
        for (Producto p : productos) {
            if (p.getId() == null) throw new DominioException("Los productos deben estar guardados antes de armar el lote");
            if (!p.getVendedorId().equals(vendedorId))
                throw new DominioException("Todos los productos deben ser del mismo vendedor");
            if (!p.estaDisponible())
                throw new DominioException("El producto '" + p.getTitulo() + "' no está disponible");
            ids.add(p.getId());
        }
        return new Lote(null, vendedorId, titulo, descripcion, precio, Estado.DISPONIBLE, ids);
    }

    /**
     * Solo informativo para la UI ("comprando suelto pagarías X"). No restringe el precio del lote.
     * Ignora productos privados (precio 0).
     */
    public static BigDecimal sumaPreciosIndividuales(Collection<Producto> productos) {
        return productos.stream()
                .filter(Producto::esPublica)
                .map(Producto::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void agregarProducto(Producto p) {
        if (!p.getVendedorId().equals(vendedorId))
            throw new DominioException("El producto no pertenece al mismo vendedor");
        if (p.getId() == null) throw new DominioException("El producto debe estar guardado");
        productoIds.add(p.getId());
    }

    public void quitarProducto(Long productoId) {
        if (!productoIds.contains(productoId)) return;
        if (productoIds.size() <= 2) throw new DominioException("Un lote necesita al menos 2 productos");
        productoIds.remove(productoId);
    }

    public boolean contiene(Long productoId) { return productoIds.contains(productoId); }

    @Override public Tipo tipo() { return Tipo.LOTE; }

    public Set<Long> getProductoIds() { return Set.copyOf(productoIds); }
}