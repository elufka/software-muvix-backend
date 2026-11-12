package com.muvix.domain.model.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public class EscrowAccount {

    public enum EscrowStatus { HELD, ACCREDITED_TO_SELLER, REFUNDED_TO_BUYER, DISPUTED }

    private final Long orderId;
    private final BigDecimal amount;
    private EscrowStatus status;
    private String externalOperationId;
    private Instant fundedAt;
    private Instant accreditedAt;

    public EscrowAccount(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = EscrowStatus.HELD;
        this.fundedAt = Instant.now();
    }

    public static EscrowAccount restore(
            Long orderId,
            BigDecimal amount,
            EscrowStatus status,
            String externalOperationId,
            Instant fundedAt,
            Instant accreditedAt) {
        EscrowAccount account = new EscrowAccount(orderId, amount);
        account.status = status;
        account.externalOperationId = externalOperationId;
        account.fundedAt = fundedAt;
        account.accreditedAt = accreditedAt;
        return account;
    }

    public void markAsAccreditedToSeller() {
        releaseToSeller(Instant.now(), Instant.now());
    }


    public void registerExternalOperationId(String operationId) {

        if (operationId == null || operationId.isBlank()) {
            throw new IllegalArgumentException(
                    "External operation ID is required"
            );
        }

        if (status != EscrowStatus.HELD) {
            throw new IllegalStateException(
                    "Cannot register operation for a non-held escrow"
            );
        }

        this.externalOperationId = operationId;
    }

    public void releaseToSeller(Instant deliveredAt, Instant now) {

        if (status != EscrowStatus.HELD) {
            throw new IllegalStateException(
                    "Escrow is not eligible for release"
            );
        }

        if (deliveredAt == null || now == null || now.isBefore(deliveredAt)) {
            throw new IllegalArgumentException(
                    "Invalid delivery timestamps"
            );
        }

        this.status = EscrowStatus.ACCREDITED_TO_SELLER;
        this.accreditedAt = now;
    }

    public void refundToBuyer() {

        if (status != EscrowStatus.HELD
                && status != EscrowStatus.DISPUTED) {
            throw new IllegalStateException(
                    "Escrow cannot be refunded in its current state"
            );
        }

        this.status = EscrowStatus.REFUNDED_TO_BUYER;
    }


    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public EscrowStatus getStatus() { return status; }
    public String getExternalOperationId() { return externalOperationId; }
    public Instant getFundedAt() { return fundedAt; }
    public Instant getAccreditedAt() { return accreditedAt; }
}