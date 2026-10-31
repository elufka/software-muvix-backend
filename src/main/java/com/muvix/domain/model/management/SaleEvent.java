package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.Location;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class SaleEvent extends RelocationEvent {
    private final Location originLocation;
    private final Instant saleDeadline;
    private final BigDecimal takeItAllPrice;
    private final List<DiscountRule> discountRules = new ArrayList<>();

    public SaleEvent(Long id, Long userId, String title, Location originLocation,
                     Instant saleDeadline, BigDecimal takeItAllPrice,
                     List<DiscountRule> rules, String shareToken, Status status) {
        super(id, userId, title, shareToken, status);
        if (originLocation == null) throw new DomainException("Origin location is required for sales");
        if (saleDeadline == null) throw new DomainException("Sale deadline is required for sales");
        if (takeItAllPrice == null || takeItAllPrice.signum() <= 0)
            throw new DomainException("Take-It-All price must be greater than zero");

        this.originLocation = originLocation;
        this.saleDeadline = saleDeadline;
        this.takeItAllPrice = takeItAllPrice;
        if (rules != null) this.discountRules.addAll(rules);
    }

    public Location getOriginLocation() { return originLocation; }
    public Instant getSaleDeadline() { return saleDeadline; }
    public BigDecimal getTakeItAllPrice() { return takeItAllPrice; }
    public List<DiscountRule> getDiscountRules() { return Collections.unmodifiableList(discountRules); }
}