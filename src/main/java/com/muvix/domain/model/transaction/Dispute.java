package com.muvix.domain.model.transaction;

import com.muvix.domain.model.event.DisputeOpenedEvent;
import com.muvix.domain.model.event.DomainEvent;
import com.muvix.domain.model.exception.DomainException;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Dispute {

    public enum Status { OPENED, UNDER_REVIEW, RESOLVED_REFUND, RESOLVED_REJECTED }

    private final Long id;
    private final Long orderId;
    private final Long buyerId;
    private final String reason;
    private final List<String> evidenceImages = new ArrayList<>();
    private Status status;
    private final Instant createdAt;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Dispute(Long id, Long orderId, Long buyerId, String reason, List<String> evidenceImages, Instant deliveredAt, Instant now) {
        if (orderId == null || buyerId == null) throw new DomainException("Order and Buyer IDs are required");
        if (reason == null || reason.isBlank()) throw new DomainException("Dispute reason is required");
        if (deliveredAt == null) throw new DomainException("Order must be delivered to open a dispute");

        long hoursElapsed = Duration.between(deliveredAt, now).toHours();
        if (hoursElapsed > 24) {
            throw new DomainException("Dispute window expired. Disputes must be opened within 24 hours of delivery.");
        }

        this.id = id;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.reason = reason;
        if (evidenceImages != null) this.evidenceImages.addAll(evidenceImages);
        this.status = Status.OPENED;
        this.createdAt = now;

        // Registro del evento de apertura de disputa
        this.domainEvents.add(new DisputeOpenedEvent(id, orderId, buyerId, reason));
    }

    public void resolve(boolean approveRefund) {
        if (status != Status.OPENED && status != Status.UNDER_REVIEW) {
            throw new DomainException("Dispute is already closed");
        }
        this.status = approveRefund ? Status.RESOLVED_REFUND : Status.RESOLVED_REJECTED;
    }

    public List<DomainEvent> getDomainEvents() { return Collections.unmodifiableList(domainEvents); }
    public void clearDomainEvents() { this.domainEvents.clear(); }

    public Long getId() { return id; }
    public Long getOrderId() { return orderId; }
    public Long getBuyerId() { return buyerId; }
    public String getReason() { return reason; }
    public List<String> getEvidenceImages() { return List.copyOf(evidenceImages); }
    public Status getStatus() { return status; }
    public Instant getCreatedAt() { return createdAt; }
}