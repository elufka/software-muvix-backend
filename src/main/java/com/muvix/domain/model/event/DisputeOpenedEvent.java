package com.muvix.domain.model.event;

import java.time.Instant;

public class DisputeOpenedEvent extends DomainEvent {
    private Long disputeId;
    private Long orderId;
    private Long buyerId;
    private String reason;

    public DisputeOpenedEvent(Long disputeId, Long orderId, Long buyerId, String reason) {
        this.disputeId = disputeId;
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.reason = reason;
    }
}
