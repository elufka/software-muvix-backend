package com.muvix.domain.model.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OrderConfirmedEvent(
    Long orderId,
    Long buyerId,
    Long eventId,
    BigDecimal totalAmount,
    Instant occurredOn
) implements DomainEvent {
    public OrderConfirmedEvent(Long orderId, Long buyerId, Long eventId, BigDecimal totalAmount) {
        this(orderId, buyerId, eventId, totalAmount, Instant.now());
    }
}
