package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;

public class PickupSlot {

    public enum SlotStatus {
        PROPOSED_BY_BUYER,
        COUNTER_PROPOSED_SELLER,
        CONFIRMED,
        REJECTED
    }

    private final Long id;
    private final Long orderId;
    private Instant proposedStartTime;
    private Instant proposedEndTime;
    private SlotStatus status;

    public PickupSlot(Long id, Long orderId, Instant startTime, Instant endTime, SlotStatus status) {
        if (startTime == null || endTime == null) throw new DomainException("Start and end time are required");
        if (endTime.isBefore(startTime)) throw new DomainException("End time cannot be before start time");
        this.id = id;
        this.orderId = orderId;
        this.proposedStartTime = startTime;
        this.proposedEndTime = endTime;
        this.status = status == null ? SlotStatus.PROPOSED_BY_BUYER : status;
    }

    public static PickupSlot requestSlot(Long orderId, Instant startTime, Instant endTime) {
        return new PickupSlot(null, orderId, startTime, endTime, SlotStatus.PROPOSED_BY_BUYER);
    }

    public void counterPropose(Instant newStartTime, Instant newEndTime) {
        if (status == SlotStatus.CONFIRMED) {
            throw new DomainException("Cannot modify an already confirmed pickup slot");
        }
        if (newEndTime.isBefore(newStartTime)) throw new DomainException("End time cannot be before start time");
        this.proposedStartTime = newStartTime;
        this.proposedEndTime = newEndTime;
        this.status = SlotStatus.COUNTER_PROPOSED_SELLER;
    }

    public void confirm() {
        if (status == SlotStatus.REJECTED) {
            throw new DomainException("Cannot confirm a rejected slot proposal");
        }
        this.status = SlotStatus.CONFIRMED;
    }

    public boolean isConfirmed() { return status == SlotStatus.CONFIRMED; }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Instant getProposedStartTime() { return proposedStartTime; }
    public Instant getProposedEndTime() { return proposedEndTime; }
    public SlotStatus getStatus() { return status; }
}
