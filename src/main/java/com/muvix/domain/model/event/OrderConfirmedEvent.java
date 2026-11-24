package com.muvix.domain.model.event;

import java.math.BigDecimal;

/** Durable order-history snapshot, including names that remain available after account erasure. */
public class OrderConfirmedEvent extends DomainEvent {
    private final Long orderId;
    private final String buyerName;
    private final String sellerName;
    private final Long eventId;
    private final BigDecimal totalAmount;

    public OrderConfirmedEvent(Long orderId, String buyerName, String sellerName,
                               Long eventId, BigDecimal totalAmount) {
        if (orderId == null) throw new IllegalArgumentException("Order ID is required");
        if (buyerName == null || buyerName.isBlank()) throw new IllegalArgumentException("Buyer name is required");
        if (sellerName == null || sellerName.isBlank()) throw new IllegalArgumentException("Seller name is required");
        this.orderId = orderId;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.eventId = eventId;
        this.totalAmount = totalAmount;
    }

    public Long getOrderId() { return orderId; }
    public String getBuyerName() { return buyerName; }
    public String getSellerName() { return sellerName; }
    public Long getEventId() { return eventId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
}