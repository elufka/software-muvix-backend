package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.enums.BoxRole;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.*;

public final class FullRelocationBox extends Box {

    private final Location originLocation;
    private final Location destinationLocation;
    private final Instant saleDeadline;
    private final Instant arrivalDeliveryDate;
    private final BigDecimal takeItAllPrice;
    private final List<DiscountRule> discountRules = new ArrayList<>();

    public FullRelocationBox(Long id, Long userId, String title, Location originLocation, Location destinationLocation,
                               Instant saleDeadline, Instant arrivalDeliveryDate, BigDecimal takeItAllPrice,
                               List<DiscountRule> rules, String shareToken, Status status, Map<Long, BoxRole> participants) {
        super(id, userId, title, shareToken, status, participants);
        if (originLocation == null || destinationLocation == null) throw new DomainException("Origin and destination are required");
        if (saleDeadline == null || arrivalDeliveryDate == null) throw new DomainException("Sale deadline and delivery date are required");
        if (takeItAllPrice == null || takeItAllPrice.signum() <= 0) throw new DomainException("Take-It-All price must be positive");

        this.originLocation = originLocation;
        this.destinationLocation = destinationLocation;
        this.saleDeadline = saleDeadline;
        this.arrivalDeliveryDate = arrivalDeliveryDate;
        this.takeItAllPrice = takeItAllPrice;
        if (rules != null) this.discountRules.addAll(rules);
    }

    public Location getOriginLocation() { return originLocation; }
    public Location getDestinationLocation() { return destinationLocation; }
    public Instant getSaleDeadline() { return saleDeadline; }
    public Instant getArrivalDeliveryDate() { return arrivalDeliveryDate; }
    public BigDecimal getTakeItAllPrice() { return takeItAllPrice; }
    public List<DiscountRule> getDiscountRules() { return Collections.unmodifiableList(discountRules); }
}
