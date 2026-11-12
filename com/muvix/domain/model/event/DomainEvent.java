package com.muvix.domain.model.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();
}
