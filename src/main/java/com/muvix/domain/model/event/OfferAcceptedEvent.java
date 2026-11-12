package com.muvix.domain.model.event;

import java.math.BigDecimal;
import java.time.Instant;

public class OfferAcceptedEvent extends DomainEvent {
    private Long offerId;
    private Long itemId;
    private Long buyerId;
    private BigDecimal amount;


    public OfferAcceptedEvent(Long offerId, Long itemId, Long buyerId, BigDecimal amount) {
        this.offerId = offerId;
        this.itemId = itemId;
        this.buyerId = buyerId;
        this.amount = amount;
    }
}
