package com.muvix.infraestructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "escrow_accounts")
public class EscrowAccountJpaEntity {

    @Id
    @Column(name = "order_id")
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EscrowStatusJpa status;

    @Column(name = "funded_at", nullable = false)
    private Instant fundedAt;

    @Column(name = "accredited_at")
    private Instant accreditedAt;

    public enum EscrowStatusJpa { HELD, ACCREDITED_TO_SELLER, DISPUTED, REFUNDED }

    // Getters y Setters
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public EscrowStatusJpa getStatus() { return status; }
    public void setStatus(EscrowStatusJpa status) { this.status = status; }
    public Instant getFundedAt() { return fundedAt; }
    public void setFundedAt(Instant fundedAt) { this.fundedAt = fundedAt; }
    public Instant getAccreditedAt() { return accreditedAt; }
    public void setAccreditedAt(Instant accreditedAt) { this.accreditedAt = accreditedAt; }
}