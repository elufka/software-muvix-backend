package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;

public record Location(String zone, String exactAddress) {

    public Location {
        if (zone == null || zone.isBlank()) throw new DomainException("Zone is required");
    }

    public Location toPublicLocation() {
        return new Location(zone, null);
    }
}
