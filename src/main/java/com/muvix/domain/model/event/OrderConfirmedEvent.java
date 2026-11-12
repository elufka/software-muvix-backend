package com.muvix.domain.model.event;

import java.math.BigDecimal;
import java.time.Instant;

public class OrderConfirmedEvent extends DomainEvent {
    private Long orderId;
    private Long buyerId;
    private Long eventId;
    private BigDecimal totalAmount;


    public OrderConfirmedEvent(Long orderId, Long buyerId, Long eventId, BigDecimal totalAmount) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
    }
}
