package com.muvix.application.port.out.event;

import com.muvix.domain.model.event.DomainEvent;
import java.util.List;

public interface EventPublisherPort {
    void publish(DomainEvent event);
    void publishAll(List<DomainEvent> events);
}