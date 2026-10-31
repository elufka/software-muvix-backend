package com.muvix.domain.model.negotiation;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;

/** Regateo estructurado: una oferta, un clic para aceptar o rechazar. Sin chat. */
public class Oferta {

    public enum Estado { PENDIENTE, ACEPTADA, RECHAZADA }

    private final Long id;
    private final Long itemId;
    private final Long compradorId;
    private final BigDecimal monto;
    private final Instant creadaEn;
    private Estado estado;

    public Oferta(Long id, Long itemId, Long compradorId, BigDecimal monto, Instant creadaEn, Estado estado) {
        this.id = id;
        this.itemId = itemId;
        this.compradorId = compradorId;
        this.monto = monto;
        this.creadaEn = creadaEn;
        this.estado = estado;
    }

    /** Solo sobre publicaciones públicas y disponibles; el comprador no puede ser el vendedor. */
    public static Oferta crear(SellableItem item, Long compradorId, BigDecimal monto, Instant ahora) {
        if (item.getSellerId().equals(compradorId))
            throw new DomainException("No podés ofertar por tus propios ítems");
        if (!item.canBeBuyed())
            throw new DomainException("El ítem no está disponible para comprar");
        if (monto == null || monto.signum() <= 0 || monto.compareTo(item.getPrice()) >= 0)
            throw new DomainException("La oferta debe ser mayor a 0 y menor al price de lista");
        return new Oferta(null, item.getId(), compradorId, monto, ahora, Estado.PENDIENTE);
    }

    public void aceptar() {
        exigirPendiente();
        estado = Estado.ACEPTADA;
    }

    public void rechazar() {
        exigirPendiente();
        estado = Estado.RECHAZADA;
    }

    private void exigirPendiente() {
        if (estado != Estado.PENDIENTE) throw new DomainException("La oferta ya fue resuelta");
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public Long getCompradorId() { return compradorId; }
    public BigDecimal getMonto() { return monto; }
    public Instant getCreadaEn() { return creadaEn; }
    public Estado getEstado() { return estado; }
}