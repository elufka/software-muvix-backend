package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "order_confirmed_events")
public class OrderConfirmedEventJpaEntity {
    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(name = "buyer_name", nullable = false, length = 200)
    private String buyerName;

    @Column(name = "seller_name", nullable = false, length = 200)
    private String sellerName;

    @Column(name = "event_id")
    private Long eventId;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "occurred_on", nullable = false)
    private Instant occurredOn;

    public OrderConfirmedEventJpaEntity() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public String getBuyerName() { return buyerName; }
    public void setBuyerName(String buyerName) { this.buyerName = buyerName; }
    public String getSellerName() { return sellerName; }
    public void setSellerName(String sellerName) { this.sellerName = sellerName; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public Instant getOccurredOn() { return occurredOn; }
    public void setOccurredOn(Instant occurredOn) { this.occurredOn = occurredOn; }
}