package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "pickup_slots")
public class PickupSlotJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "order_id", nullable = false)
    private Long orderId;
    @Column(name = "start_time", nullable = false)
    private Instant startTime;
    @Column(name = "end_time", nullable = false)
    private Instant endTime;
    @Enumerated(EnumType.STRING) @Column(nullable = false)
    private com.muvix.domain.model.logistic.PickupSlot.SlotStatus status;
    @Column(name = "buyer_confirmed", nullable = false)
    private boolean buyerConfirmed;
    @Column(name = "seller_confirmed", nullable = false)
    private boolean sellerConfirmed;
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Instant getStartTime() { return startTime; }
    public void setStartTime(Instant startTime) { this.startTime = startTime; }
    public Instant getEndTime() { return endTime; }
    public void setEndTime(Instant endTime) { this.endTime = endTime; }
    public com.muvix.domain.model.logistic.PickupSlot.SlotStatus getStatus() { return status; }
    public void setStatus(com.muvix.domain.model.logistic.PickupSlot.SlotStatus status) { this.status = status; }
    public boolean isBuyerConfirmed() { return buyerConfirmed; }
    public void setBuyerConfirmed(boolean buyerConfirmed) { this.buyerConfirmed = buyerConfirmed; }
    public boolean isSellerConfirmed() { return sellerConfirmed; }
    public void setSellerConfirmed(boolean sellerConfirmed) { this.sellerConfirmed = sellerConfirmed; }
}
