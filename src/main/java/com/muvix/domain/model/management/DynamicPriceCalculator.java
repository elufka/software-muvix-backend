package com.muvix.domain.model.management;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

public class DynamicPriceCalculator {

    public BigDecimal calculate(SellableItem item, Instant saleDeadline, List<DiscountRule> rules, Instant now) {
        if (!item.isPublic()) return BigDecimal.ZERO;
        if (now.isAfter(saleDeadline)) {
            throw new DomainException("Sales period has expired");
        }

        long daysRemaining = Duration.between(now, saleDeadline).toDays();
        BigDecimal maxPercentage = BigDecimal.ZERO;

        for (DiscountRule rule : rules) {
            if (daysRemaining <= rule.daysLeft()) {
                if (rule.percentage().compareTo(maxPercentage) > 0) {
                    maxPercentage = rule.percentage();
                }
            }
        }

        if (maxPercentage.signum() == 0) return item.getPrice();

        BigDecimal discountFactor = BigDecimal.ONE.subtract(
                maxPercentage.divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
        );
        return item.getPrice().multiply(discountFactor).setScale(2, RoundingMode.HALF_UP);
    }
}
