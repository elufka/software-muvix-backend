package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.Location;

import java.time.Instant;

public final class PurchaseEvent extends RelocationEvent {
    private final Location destinationLocation;
    private final Instant arrivalDeliveryDate;

    public PurchaseEvent(Long id, Long userId, String title, Location destinationLocation,
                         Instant arrivalDeliveryDate, String shareToken, Status status) {
        super(id, userId, title, shareToken, status);
        if (destinationLocation == null) throw new DomainException("Destination location is required");
        if (arrivalDeliveryDate == null) throw new DomainException("Arrival delivery date is required");

        this.destinationLocation = destinationLocation;
        this.arrivalDeliveryDate = arrivalDeliveryDate;
    }

    public Location getDestinationLocation() { return destinationLocation; }
    public Instant getArrivalDeliveryDate() { return arrivalDeliveryDate; }
}