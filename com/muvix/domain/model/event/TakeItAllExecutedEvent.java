package com.muvix.domain.model.event;

import java.math.BigDecimal;
import java.time.Instant;

public record TakeItAllExecutedEvent(
    Long eventId,
    Long buyerId,
    Long sellerId,
    BigDecimal totalPrice,
    Instant occurredOn
) implements DomainEvent {
    public TakeItAllExecutedEvent(Long eventId, Long buyerId, Long sellerId, BigDecimal totalPrice) {
        this(eventId, buyerId, sellerId, totalPrice, Instant.now());
    }
}
