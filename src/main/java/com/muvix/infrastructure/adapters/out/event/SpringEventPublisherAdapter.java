package com.muvix.infrastructure.adapters.out.event;

import com.muvix.application.port.out.event.EventPublisherPort;
import com.muvix.domain.model.event.DomainEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
public class SpringEventPublisherAdapter implements EventPublisherPort {

    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringEventPublisherAdapter(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainEvent event) {
        if (event != null) {
            applicationEventPublisher.publishEvent(event);
        }
    }

    @Override
    public void publishAll(List<DomainEvent> events) {
        if (events != null) {
            events.forEach(this::publish);
        }
    }
}
