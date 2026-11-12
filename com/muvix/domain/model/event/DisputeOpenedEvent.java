package com.muvix.domain.model.event;

import java.time.Instant;

public record DisputeOpenedEvent(
    Long disputeId,
    Long orderId,
    Long buyerId,
    String reason,
    Instant occurredOn
) implements DomainEvent {
    public DisputeOpenedEvent(Long disputeId, Long orderId, Long buyerId, String reason) {
        this(disputeId, orderId, buyerId, reason, Instant.now());
    }
}
