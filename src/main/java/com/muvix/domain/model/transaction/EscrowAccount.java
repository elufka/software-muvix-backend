package com.muvix.domain.model.transaction;

import java.math.BigDecimal;
import java.time.Instant;

public class EscrowAccount {

    public enum EscrowStatus { PENDING_PAYMENT, HELD, ACCREDITED_TO_SELLER, REFUNDED_TO_BUYER, DISPUTED, CANCELLED }

    private final Long orderId;
    private final BigDecimal amount;
    private EscrowStatus status;
    private String externalOperationId;
    private String checkoutUrl;
    private Instant fundedAt;
    private Instant accreditedAt;

    public EscrowAccount(Long orderId, BigDecimal amount) {
        this.orderId = orderId;
        this.amount = amount;
        this.status = EscrowStatus.PENDING_PAYMENT;
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

    public static EscrowAccount restore(Long orderId,BigDecimal amount,EscrowStatus status,String externalOperationId,
                                        Instant fundedAt,Instant accreditedAt,String checkoutUrl){
        EscrowAccount account=restore(orderId,amount,status,externalOperationId,fundedAt,accreditedAt);
        account.checkoutUrl=checkoutUrl;return account;
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

        if (status != EscrowStatus.PENDING_PAYMENT) {
            throw new IllegalStateException(
                    "Payment can only be registered for a pending escrow"
            );
        }
        this.externalOperationId = operationId;
        this.status = EscrowStatus.HELD;
        this.fundedAt = Instant.now();
    }

    public void releaseToSeller(Instant deliveredAt, Instant now) {

        if (status != EscrowStatus.HELD && status != EscrowStatus.DISPUTED) {
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

    public void markDisputed() {
        if (status != EscrowStatus.HELD) {
            throw new IllegalStateException("Only held funds can be marked as disputed");
        }
        this.status = EscrowStatus.DISPUTED;
    }

    public void attachExternalOperationId(String operationId) {
        if (operationId == null || operationId.isBlank()) throw new IllegalArgumentException("External operation ID is required");
        if (status != EscrowStatus.PENDING_PAYMENT) throw new IllegalStateException("Only pending escrow can be linked to a checkout");
        this.externalOperationId = operationId;
    }

    public void attachCheckout(String operationId,String checkoutUrl){
        attachExternalOperationId(operationId);
        if(checkoutUrl==null||checkoutUrl.isBlank())throw new IllegalArgumentException("Checkout URL is required");
        this.checkoutUrl=checkoutUrl;
    }

    public void cancelUnpaid() {
        if (status != EscrowStatus.PENDING_PAYMENT) throw new IllegalStateException("Only unpaid escrow can be cancelled without refund");
        status = EscrowStatus.CANCELLED;
    }

    public void refundLatePayment(String operationId) {
        if (status != EscrowStatus.CANCELLED) throw new IllegalStateException("Only a cancelled unpaid escrow can receive a late payment");
        if (operationId == null || operationId.isBlank()) throw new IllegalArgumentException("External operation ID is required");
        externalOperationId = operationId;
        status = EscrowStatus.REFUNDED_TO_BUYER;
        fundedAt = Instant.now();
    }


    public Long getOrderId() { return orderId; }
    public BigDecimal getAmount() { return amount; }
    public EscrowStatus getStatus() { return status; }
    public String getExternalOperationId() { return externalOperationId; }
    public String getCheckoutUrl() { return checkoutUrl; }
    public Instant getFundedAt() { return fundedAt; }
    public Instant getAccreditedAt() { return accreditedAt; }
}