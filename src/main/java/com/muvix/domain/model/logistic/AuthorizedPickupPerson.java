package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;

public record AuthorizedPickupPerson(String fullName, String documentId, String phone) {

    public AuthorizedPickupPerson {
        if (fullName == null || fullName.isBlank()) throw new DomainException("Authorized person full name is required");
        if (documentId == null || documentId.isBlank()) throw new DomainException("Document ID is required");
    }
}
