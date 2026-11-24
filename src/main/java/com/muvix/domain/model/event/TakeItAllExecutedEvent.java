package com.muvix.domain.model.event;

import java.math.BigDecimal;

public class TakeItAllExecutedEvent extends DomainEvent {

    private Long eventId;
    private Long buyerId;
    private Long sellerId;
    private BigDecimal totalPrice;

    public TakeItAllExecutedEvent(Long eventId, Long buyerId, Long sellerId, BigDecimal totalPrice) {
        this.eventId = eventId;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.totalPrice = totalPrice;
    }
}
