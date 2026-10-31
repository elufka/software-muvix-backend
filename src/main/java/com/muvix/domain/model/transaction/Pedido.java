package com.muvix.domain.model.transaction;

import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

public class Pedido {

    public enum Origen { COMPRA_DIRECTA, OFERTA_ACEPTADA, TAKE_IT_ALL }
    public enum Estado { PENDIENTE_TURNO, TURNO_CONFIRMADO, RETIRADO, CANCELADO }

    private final Long id;
    private final Long eventoId; // ID de cualquier subclasificación de EventoMudanza
    private final Long compradorId;
    private final Set<Long> itemIds = new HashSet<>();
    private final BigDecimal total;
    private final Origen origen;
    private Long turnoId;
    private Estado estado;

    public Pedido(Long id, Long eventoId, Long compradorId, Set<Long> itemIds, BigDecimal total, Origen origen, Long turnoId, Estado estado) {
        if (eventoId == null || compradorId == null) throw new DomainException("Evento y Comprador son obligatorios");
        if (itemIds == null || itemIds.isEmpty()) throw new DomainException("El pedido debe contener al menos un ítem");
        if (total == null || total.signum() <= 0) throw new DomainException("Monto total inválido");

        this.id = id;
        this.eventoId = eventoId;
        this.compradorId = compradorId;
        this.itemIds.addAll(itemIds);
        this.total = total;
        this.origen = origen;
        this.turnoId = turnoId;
        this.estado = estado == null ? Estado.PENDIENTE_TURNO : estado;
    }

    public void confirmarTurno(Long turnoId) {
        if (estado != Estado.PENDIENTE_TURNO) throw new DomainException("El pedido no admite selección de turno en su state actual");
        this.turnoId = turnoId;
        this.estado = Estado.TURNO_CONFIRMADO;
    }

    public boolean puedeVerDireccionExacta() {
        return estado == Estado.TURNO_CONFIRMADO || estado == Estado.RETIRADO;
    }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public Long getCompradorId() { return compradorId; }
    public Set<Long> getItemIds() { return Set.copyOf(itemIds); }
    public BigDecimal getTotal() { return total; }
    public Origen getOrigen() { return origen; }
    public Long getTurnoId() { return turnoId; }
    public Estado getEstado() { return estado; }
}