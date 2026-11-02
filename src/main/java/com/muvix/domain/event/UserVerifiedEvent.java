package com.muvix.domain.event;

import java.time.Instant;

public record UserVerifiedEvent(
        Long userId,
        String verificationTier,
        Instant occurredOn
) implements DomainEvent {
    public UserVerifiedEvent(Long userId, String verificationTier) {
        this(userId, verificationTier, Instant.now());
    }
}