package com.muvix.domain.model.catalogue;

import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;

/**
 * Publicación: un Product o un Batch. Mapea a la tabla items_vendibles (columna tipo_item).
 *
 * Visibilidad derivada del price:
 *   price == 0  -> PRIVADA (existe en el inventario del vendedor, no se puede comprar ni ver en el catálogo)
 *   price  > 0  -> PÚBLICA (se puede comprar / ofertar)
 * Pasar de privada a pública es simplemente asignarle un price.
 *
 * El price de un Batch es independiente del de sus productos: no hay relación obligada.
 */
public abstract sealed class SellableItem permits Product, Batch {

    public enum Type { PRODUCT, BATCH }

    public enum State { AVAILABLE, RESERVED, SOLD}

    protected final Long id;
    protected final Long sellerId;   // seller_id
    protected String title;
    protected String description;
    protected BigDecimal price;       // NOT NULL, 0 = privada
    protected State state;

    protected SellableItem(Long id, Long sellerId, String title, String description,
                           BigDecimal price, State state) {
        if (sellerId == null) throw new DomainException("El vendedor es obligatorio");
        if (title == null || title.isBlank()) throw new DomainException("El título es obligatorio");
        this.id = id;
        this.sellerId = sellerId;
        this.title = title;
        this.description = description;
        this.price = normalizarPrecio(price);
        this.state = state == null ? State.AVAILABLE : state;
    }

    /** null o 0 => privada. Negativo => error. */
    protected static BigDecimal normalizarPrecio(BigDecimal precio) {
        if (precio == null) return BigDecimal.ZERO;
        if (precio.signum() < 0) throw new DomainException("El price no puede ser negativo");
        return precio;
    }

    public abstract Type type();

    // ---- visibilidad / price ----

    public boolean isPublic() { return price.signum() > 0; }

    /** Privada -> pública (o cambia el price de una ya pública). */
    public void publicate(BigDecimal nuevoPrecio) {
        if (nuevoPrecio == null || nuevoPrecio.signum() <= 0)
            throw new DomainException("Para publicate hace falta un price mayor a 0");
        this.price = nuevoPrecio;
    }

    /** Pública -> privada. */
    public void makePrivate() { this.price = BigDecimal.ZERO; }

    // ---- disponibilidad ----

    public boolean isAvailable() { return state == State.AVAILABLE; }

    /** Solo se compra / oferta lo que es público y está disponible. */
    public boolean canBeBuyed() { return isPublic() && isAvailable(); }

    public void reserve() {
        if (!isAvailable()) throw new DomainException("El ítem ya no está disponible");
        state = State.RESERVED;
    }

    public void markAsSold() {
        if (state == State.SOLD) throw new DomainException("El ítem ya fue vendido");
        state = State.SOLD;
    }

    public void free() {
        if (state == State.SOLD) throw new DomainException("Un ítem vendido no se puede free");
        state = State.AVAILABLE;
    }

    public Long getId() { return id; }
    public Long getSellerId() { return sellerId; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public BigDecimal getPrice() { return price; }
    public State getState() { return state; }
}