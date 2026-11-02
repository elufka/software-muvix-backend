package com.muvix.domain.model.user;

import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;

public class VerificationLevel {

    public enum Tier {
        UNVERIFIED(new BigDecimal("100.00")),
        BASIC_2FA(new BigDecimal("1000.00")),
        IDENTITY_VERIFIED(new BigDecimal("10000.00"));

        private final BigDecimal maxTransactionLimit;

        Tier(BigDecimal maxTransactionLimit) {
            this.maxTransactionLimit = maxTransactionLimit;
        }

        public BigDecimal getMaxTransactionLimit() { return maxTransactionLimit; }
    }

    private final Long userId;
    private Tier tier;
    private Instant verifiedAt;

    public VerificationLevel(Long userId, Tier tier, Instant verifiedAt) {
        if (userId == null) throw new DomainException("User ID is required");
        this.userId = userId;
        this.tier = tier == null ? Tier.UNVERIFIED : tier;
        this.verifiedAt = verifiedAt;
    }

    public void upgrade(Tier newTier) {
        if (newTier.ordinal() <= this.tier.ordinal()) {
            throw new DomainException("Cannot downgrade or re-assign current verification level");
        }
        this.tier = newTier;
        this.verifiedAt = Instant.now();
    }

    public boolean canTransact(BigDecimal amount) {
        return amount != null && amount.compareTo(tier.getMaxTransactionLimit()) <= 0;
    }

    public Long getUserId() { return userId; }
    public Tier getTier() { return tier; }
    public Instant getVerifiedAt() { return verifiedAt; }
}
