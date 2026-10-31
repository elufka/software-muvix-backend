package com.muvix.domain.model;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

/**
 * Compra confirmada de uno o más ítems. Nace de: compra directa, oferta aceptada
 * o "Take It All". Queda pendiente hasta que el comprador elige turno de retiro.
 */
public class Pedido {

    public enum Origen { COMPRA_DIRECTA, OFERTA_ACEPTADA, TAKE_IT_ALL }

    public enum Estado { PENDIENTE_TURNO, TURNO_CONFIRMADO, RETIRADO, CANCELADO }

    private final Long id;
    private final Long eventoId;
    private final Long compradorId;
    private final Set<Long> itemIds = new HashSet<>();
    private final BigDecimal total;
    private final Origen origen;
    private Long turnoId;
    private Estado estado;

    public Pedido(Long id, Long eventoId, Long compradorId, Set<Long> itemIds, BigDecimal total,
                  Origen origen, Long turnoId, Estado estado) {
        this.id = id;
        this.eventoId = eventoId;
        this.compradorId = compradorId;
        this.itemIds.addAll(itemIds);
        this.total = total;
        this.origen = origen;
        this.turnoId = turnoId;
        this.estado = estado;
    }

    public static Pedido crear(Long eventoId, Long compradorId, Set<Long> itemIds,
                               BigDecimal total, Origen origen) {
        if (itemIds == null || itemIds.isEmpty()) throw new DominioException("El pedido no tiene ítems");
        if (total == null || total.signum() <= 0) throw new DominioException("Total inválido");
        return new Pedido(null, eventoId, compradorId, itemIds, total, origen, null, Estado.PENDIENTE_TURNO);
    }

    public void confirmarTurno(Long turnoId) {
        if (estado != Estado.PENDIENTE_TURNO) throw new DominioException("El pedido no admite elegir turno");
        this.turnoId = turnoId;
        this.estado = Estado.TURNO_CONFIRMADO;
    }

    public void marcarRetirado() {
        if (estado != Estado.TURNO_CONFIRMADO) throw new DominioException("Falta confirmar el turno");
        estado = Estado.RETIRADO;
    }

    public void cancelar() {
        if (estado == Estado.RETIRADO) throw new DominioException("El pedido ya fue retirado");
        estado = Estado.CANCELADO;
    }

    /** La dirección exacta y el contacto solo se revelan con turno confirmado. */
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
