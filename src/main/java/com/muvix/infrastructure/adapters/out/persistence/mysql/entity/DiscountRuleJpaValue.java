package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.math.BigDecimal;

@Embeddable
public class DiscountRuleJpaValue {

    @Column(name = "days_left", nullable = false)
    private int daysLeft;

    @Column(name = "percentage", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentage;

    public DiscountRuleJpaValue() {}

    public DiscountRuleJpaValue(int daysLeft, BigDecimal percentage) {
        this.daysLeft = daysLeft;
        this.percentage = percentage;
    }

    public int getDaysLeft() { return daysLeft; }
    public void setDaysLeft(int daysLeft) { this.daysLeft = daysLeft; }
    public BigDecimal getPercentage() { return percentage; }
    public void setPercentage(BigDecimal percentage) { this.percentage = percentage; }
}
