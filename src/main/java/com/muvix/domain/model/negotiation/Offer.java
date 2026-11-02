package com.muvix.domain.model.negotiation;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.event.DomainEvent;
import com.muvix.domain.model.event.OfferAcceptedEvent;
import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Offer {

    public enum Status { PENDING, ACCEPTED, REJECTED }

    private final Long id;
    private final Long itemId;
    private final Long buyerId;
    private final BigDecimal amount;
    private final Instant createdAt;
    private Status status;

    private final List<DomainEvent> domainEvents = new ArrayList<>();

    public Offer(Long id, Long itemId, Long buyerId, BigDecimal amount, Instant createdAt, Status status) {
        this.id = id;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.amount = amount;
        this.createdAt = createdAt;
        this.status = status;
    }

    public static Offer create(SellableItem item, Long buyerId, BigDecimal amount, Instant now) {
        if (item.getSellerId().equals(buyerId)) {
            throw new DomainException("You cannot make an offer on your own items");
        }
        if (!item.canBePurchased()) {
            throw new DomainException("Item is not available for purchase");
        }
        if (amount == null || amount.signum() <= 0 || amount.compareTo(item.getPrice()) >= 0) {
            throw new DomainException("Offer must be greater than zero and less than list price");
        }
        return new Offer(null, item.getId(), buyerId, amount, now, Status.PENDING);
    }

    public void accept() {
        requirePending();
        this.status = Status.ACCEPTED;
        // Registro del evento de dominio
        this.domainEvents.add(new OfferAcceptedEvent(id, itemId, buyerId, amount));
    }

    public void reject() {
        requirePending();
        this.status = Status.REJECTED;
    }

    private void requirePending() {
        if (status != Status.PENDING) throw new DomainException("Offer has already been resolved");
    }

    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }
    public void clearDomainEvents() {
        this.domainEvents.clear();
    }

    public Long getId() { return id; }
    public Long getItemId() { return itemId; }
    public Long getBuyerId() { return buyerId; }
    public BigDecimal getAmount() { return amount; }
    public Instant getCreatedAt() { return createdAt; }
    public Status getStatus() { return status; }
}