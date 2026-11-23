package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;

public class PickupSlot {

    public enum SlotStatus {
        PROPOSED_BY_BUYER,
        PROPOSED_BY_SELLER,
        COUNTER_PROPOSED_SELLER,
        CONFIRMED,
        REJECTED
    }

    private final Long id;
    private final Long orderId;
    private Instant proposedStartTime;
    private Instant proposedEndTime;
    private SlotStatus status;
    private boolean buyerConfirmed;
    private boolean sellerConfirmed;

    public PickupSlot(Long id, Long orderId, Instant startTime, Instant endTime, SlotStatus status) {
        if (orderId == null) throw new DomainException("Order ID is required");
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

    public void counterPropose(Instant newStartTime, Instant newEndTime, Long proposerId, Long buyerId) {
        if (status == SlotStatus.CONFIRMED) {
            throw new DomainException("Cannot modify an already confirmed pickup slot");
        }
        if (newStartTime == null || newEndTime == null) throw new DomainException("Start and end time are required");
        if (newEndTime.isBefore(newStartTime)) throw new DomainException("End time cannot be before start time");
        if (proposerId == null || buyerId == null) throw new DomainException("Pickup slot proposer is required");
        this.proposedStartTime = newStartTime;
        this.proposedEndTime = newEndTime;
        this.status = proposerId.equals(buyerId) ? SlotStatus.PROPOSED_BY_BUYER : SlotStatus.PROPOSED_BY_SELLER;
        this.buyerConfirmed = false;
        this.sellerConfirmed = false;
    }

    public void confirm(Long userId, Long buyerId, Long sellerId) {
        if (status == SlotStatus.REJECTED) {
            throw new DomainException("Cannot confirm a rejected slot proposal");
        }
        if (userId == null || buyerId == null || sellerId == null
                || (!userId.equals(buyerId) && !userId.equals(sellerId)))
            throw new DomainException("Only order participants can confirm a pickup slot");
        if (userId.equals(buyerId)) buyerConfirmed = true;
        if (userId.equals(sellerId)) sellerConfirmed = true;
        if (buyerConfirmed && sellerConfirmed) this.status = SlotStatus.CONFIRMED;
    }

    public boolean isConfirmed() { return status == SlotStatus.CONFIRMED; }
    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Instant getProposedStartTime() { return proposedStartTime; }
    public Instant getProposedEndTime() { return proposedEndTime; }
    public SlotStatus getStatus() { return status; }
    public boolean isBuyerConfirmed() { return buyerConfirmed; }
    public boolean isSellerConfirmed() { return sellerConfirmed; }
    public static PickupSlot restore(Long id, Long orderId, Instant start, Instant end, SlotStatus status,
                                     boolean buyerConfirmed, boolean sellerConfirmed) {
        PickupSlot slot = new PickupSlot(id, orderId, start, end, status);
        slot.buyerConfirmed = buyerConfirmed;
        slot.sellerConfirmed = sellerConfirmed;
        return slot;
    }
}
