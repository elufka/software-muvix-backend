package com.muvix.domain.model;

import java.time.Instant;

/** Bloque horario que el vendedor deja disponible para retiros. Lo reserva un Pedido. */
public class TurnoRetiro {

    private final Long id;
    private final Long eventoId;
    private final Instant inicio;
    private final Instant fin;
    private Long pedidoId; // null = libre

    public TurnoRetiro(Long id, Long eventoId, Instant inicio, Instant fin, Long pedidoId) {
        this.id = id;
        this.eventoId = eventoId;
        this.inicio = inicio;
        this.fin = fin;
        this.pedidoId = pedidoId;
    }

    /** Valida contra la fecha límite del evento: no se puede ofrecer un turno posterior a la entrega de llaves. */
    public static TurnoRetiro crear(EventoMudanza evento, Instant inicio, Instant fin) {
        if (!fin.isAfter(inicio)) throw new DominioException("El fin debe ser posterior al inicio");
        if (fin.isAfter(evento.getFechaLimite()))
            throw new DominioException("El turno no puede superar la fecha límite del evento");
        return new TurnoRetiro(null, evento.getId(), inicio, fin, null);
    }

    public boolean estaLibre() { return pedidoId == null; }

    public void reservar(Long pedidoId) {
        if (!estaLibre()) throw new DominioException("El turno ya fue reservado");
        this.pedidoId = pedidoId;
    }

    public void liberar() { this.pedidoId = null; }

    public Long getId() { return id; }
    public Long getEventoId() { return eventoId; }
    public Instant getInicio() { return inicio; }
    public Instant getFin() { return fin; }
    public Long getPedidoId() { return pedidoId; }
}
