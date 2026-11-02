package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;

public record DiscountRule(int daysLeft, BigDecimal percentage) {

    public DiscountRule {
        if (daysLeft < 0) throw new DomainException("Days left cannot be negative");
        if (percentage == null
                || percentage.compareTo(BigDecimal.ZERO) <= 0
                || percentage.compareTo(new BigDecimal("100")) >= 0) {
            throw new DomainException("Percentage must be between 0 and 100 (exclusive)");
        }
    }
}
