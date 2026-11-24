package com.muvix.domain.model.logistic;

import java.time.Instant;

public record ChatMessageTemplate(Long id, Type type, String title, String message,
                                  Long createdBy, Instant createdAt, boolean active) {
    public enum Type { DATETIME, PRICE }
}