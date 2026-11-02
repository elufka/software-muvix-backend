package com.muvix.domain.model.transaction;

import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;

public class EscrowAccount {

    public enum EscrowStatus { HELD, ACCREDITED_TO_SELLER, DISPUTED, REFUNDED }

    private final Long orderId;
    private final BigDecimal amount;
    private EscrowStatus status;
    private final Instant fundedAt;
    private Instant accreditedAt;

    public EscrowAccount(Long orderId, BigDecimal amount) {
        if (orderId == null || amount == null || amount.signum() <= 0) {
            throw new DomainException("Valid Order ID and amount are required for Escrow");
        }
        this.orderId = orderId;
        this.amount = amount;
        this.status = EscrowStatus.HELD;
        this.fundedAt = Instant.now();
    }

    public void releaseToSeller(Instant deliveredAt, Instant now) {
        if (status != EscrowStatus.HELD) throw new DomainException("Escrow funds are not in held status");
        if (deliveredAt == null) throw new DomainException("Delivery timestamp is required for release");

        long hoursSinceDelivery = Duration.between(deliveredAt, now).toHours();
        if (hoursSinceDelivery < 24) {
            throw new DomainException("Cannot accredit funds prior to the 24-hour window ending");
        }
        this.status = EscrowStatus.ACCREDITED_TO_SELLER;
        this.accreditedAt = now;
    }

    public void freezeForDispute() {
        if (status != EscrowStatus.HELD) throw new DomainException("Escrow cannot be frozen for dispute in current status");
        this.status = EscrowStatus.DISPUTED;
    }

    public void refundToBuyer() {
        if (status != EscrowStatus.DISPUTED) throw new DomainException("Refund requires an active dispute state");
        this.status = EscrowStatus.REFUNDED;
    }

    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public EscrowStatus getStatus() { return status; }
    public Instant getFundedAt() { return fundedAt; }
    public Instant getAccreditedAt() { return accreditedAt; }
}