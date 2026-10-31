package com.muvix.domain.logistics;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;

public class PickupSlot {

    public enum SlotStatus {
        PROPOSED_BY_BUYER,       // Esperando respuesta del vendedor
        COUNTER_PROPOSED_SELLER, // Esperando respuesta del comprador
        CONFIRMED,               // Turno cerrado y fijado
        REJECTED                 // Propuesta rechazada
    }

    private final Long id;
    private final Long orderId;
    private Instant proposedStartTime;
    private Instant proposedEndTime;
    private SlotStatus status;

    public PickupSlot(Long id, Long orderId, Instant startTime, Instant endTime, SlotStatus proposedByBuyer) {
        this.id = id;
        this.orderId = orderId;
    }
// --- Métodos de Transición de Estado ---

    /** El comprador propone un horario inicial */
    public static PickupSlot requestSlot(Long orderId, Instant startTime, Instant endTime) {
        return new PickupSlot(null, orderId, startTime, endTime, SlotStatus.PROPOSED_BY_BUYER);
    }

    /** El vendedor realiza una contrapropuesta con un nuevo horario */
    public void counterPropose(Instant newStartTime, Instant newEndTime) {
        if (status == SlotStatus.CONFIRMED) {
            throw new DomainException("Cannot modify an already confirmed pickup slot");
        }
        this.proposedStartTime = newStartTime;
        this.proposedEndTime = newEndTime;
        this.status = SlotStatus.COUNTER_PROPOSED_SELLER;
    }

    /** Cualquiera de las partes acepta la propuesta activa */
    public void confirm() {
        if (status == SlotStatus.REJECTED) {
            throw new DomainException("Cannot confirm a rejected slot proposal");
        }
        this.status = SlotStatus.CONFIRMED;
    }

    public boolean isConfirmed() { return status == SlotStatus.CONFIRMED; }
    public Instant getProposedStartTime() { return proposedStartTime; }
    public SlotStatus getStatus() { return status; }
}