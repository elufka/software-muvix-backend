package com.muvix.domain.event;

import java.math.BigDecimal;
import java.time.Instant;

public record OfferAcceptedEvent(
        Long offerId,
        Long itemId,
        Long buyerId,
        BigDecimal amount,
        Instant occurredOn
) implements DomainEvent {
    public OfferAcceptedEvent(Long offerId, Long itemId, Long buyerId, BigDecimal amount) {
        this(offerId, itemId, buyerId, amount, Instant.now());
    }
}
