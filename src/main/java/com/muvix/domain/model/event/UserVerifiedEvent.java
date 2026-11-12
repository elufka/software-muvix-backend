package com.muvix.domain.model.event;

import java.time.Instant;

public class UserVerifiedEvent extends DomainEvent {

    private Long userId;
    private String verificationTier;


    public UserVerifiedEvent(Long userId, String verificationTier) {
        this.userId = userId;
        this.verificationTier = verificationTier;
    }
}
