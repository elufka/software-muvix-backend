package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import java.util.UUID;

public abstract sealed class RelocationEvent permits SaleEvent, PurchaseEvent, FullRelocationEvent {

    public enum Status { DRAFT, ACTIVE, COMPLETED, CANCELLED }

    protected final Long id;
    protected final Long userId;
    protected String title;
    protected final String uniqueShareToken;
    protected Status status;

    protected RelocationEvent(Long id, Long userId, String title, String uniqueShareToken, Status status) {
        if (userId == null) throw new DomainException("User ID is required");
        if (title == null || title.isBlank()) throw new DomainException("Event title is required");

        this.id = id;
        this.userId = userId;
        this.title = title;
        this.uniqueShareToken = (uniqueShareToken != null && !uniqueShareToken.isBlank())
                ? uniqueShareToken
                : UUID.randomUUID().toString().substring(0, 8);
        this.status = status == null ? Status.DRAFT : status;
    }

    public void complete() { this.status = Status.COMPLETED; }
    public boolean isActive() { return status == Status.ACTIVE; }

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getTitle() { return title; }
    public String getUniqueShareToken() { return uniqueShareToken; }
    public Status getStatus() { return status; }
}