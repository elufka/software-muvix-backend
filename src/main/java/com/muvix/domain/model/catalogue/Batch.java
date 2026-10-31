package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Publicación que agrupa productos bajo un price PROPIO, sin relación con la suma de los
 * precios individuales (puede ser mayor, menor o igual). price 0 => lote privado.
 *
 * Un producto puede estar en N lotes. Solo contains productos (no lotes anidados).
 *
 * Cómo se crea (lo orquesta el caso de uso, no el dominio):
 *  - desde productos existentes: se pasan sus ids
 *  - desde cero: el caso de uso crea primero los productos (públicos con price, o privados
 *    con price 0/null) y los persiste; luego llama a Batch.crear con los productos ya con id.
 */
public final class Batch extends SellableItem {

    private final Set<Long> productIds = new HashSet<>();

    public Batch(Long id, Long sellerId, String titulo, String descripcion,
                 BigDecimal precio, State state, Set<Long> productoIds) {
        super(id, sellerId, titulo, descripcion, precio, state);
        if (productoIds != null) this.productIds.addAll(productoIds);
    }

    public static Batch crear(Long sellerId, String titulo, String descripcion,
                              Collection<Product> products, BigDecimal precio) {
        if (products == null || products.size() < 2)
            throw new DomainException("Un lote necesita al menos 2 products");

        Set<Long> ids = new HashSet<>();
        for (Product p : products) {
            if (p.getId() == null) throw new DomainException("Los products deben estar guardados antes de armar el lote");
            if (!p.getSellerId().equals(sellerId))
                throw new DomainException("Todos los products deben ser del mismo vendedor");
            if (!p.isAvailable())
                throw new DomainException("El producto '" + p.getTitle() + "' no está disponible");
            ids.add(p.getId());
        }
        return new Batch(null, sellerId, titulo, descripcion, precio, State.AVAILABLE, ids);
    }

    /**
     * Solo informativo para la UI ("comprando suelto pagarías X"). No restringe el price del lote.
     * Ignora products privados (price 0).
     */
    public static BigDecimal sumaPreciosIndividuales(Collection<Product> products) {
        return products.stream()
                .filter(Product::isPublic)
                .map(Product::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addProduct(Product p) {
        if (!p.getSellerId().equals(sellerId))
            throw new DomainException("El producto no pertenece al mismo vendedor");
        if (p.getId() == null) throw new DomainException("El producto debe estar guardado");
        productIds.add(p.getId());
    }

    public void deleteProduct(Long productoId) {if (!productIds.contains(productoId)) return;
        if (productIds.size() <= 2) throw new DomainException("Un lote necesita al menos 2 productos");
        productIds.remove(productoId);
    }

    public boolean contains(Long productId) { return productIds.contains(productId); }

    @Override public Type type() { return Type.BATCH; }

    public Set<Long> getProductIds() { return Set.copyOf(productIds); }
}