package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.enums.BoxRole;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

public final class PurchaseBox extends Box {

    private final Location destinationLocation;
    private final Instant arrivalDeliveryDate;

    public PurchaseBox(Long id, Long userId, String title, Location destinationLocation,
                       Instant arrivalDeliveryDate, String shareToken, Status status,
                       Map<Long, BoxRole> participants) {
        super(id, userId, title, shareToken, status, participants);
        if (destinationLocation == null) throw new DomainException("Destination location is required");
        if (arrivalDeliveryDate == null) throw new DomainException("Arrival delivery date is required");

        this.destinationLocation = destinationLocation;
        this.arrivalDeliveryDate = arrivalDeliveryDate;
    }

    public Location getDestinationLocation() { return destinationLocation; }
    public Instant getArrivalDeliveryDate() { return arrivalDeliveryDate; }
}
