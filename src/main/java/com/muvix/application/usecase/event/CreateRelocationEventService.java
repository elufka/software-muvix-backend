package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.CreateRelocationEventUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.management.FullRelocationEvent;
import com.muvix.domain.model.management.PurchaseEvent;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.SaleEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateRelocationEventService implements CreateRelocationEventUseCase {

    private final RelocationEventRepositoryPort eventRepository;

    public CreateRelocationEventService(RelocationEventRepositoryPort eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    @Transactional
    public RelocationEvent createSaleBox(CreateSaleBoxCommand command) {
        SaleEvent event = new SaleEvent(
                null,
                command.userId(),
                command.title(),
                command.originLocation(),
                command.saleDeadline(),
                command.takeItAllPrice(),
                command.discountRules(),
                null,
                RelocationEvent.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public RelocationEvent createPurchaseBox(CreatePurchaseBoxCommand command) {
        PurchaseEvent event = new PurchaseEvent(
                null,
                command.userId(),
                command.title(),
                command.destinationLocation(),
                command.arrivalDeliveryDate(),
                null,
                RelocationEvent.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public RelocationEvent createFullRelocationBox(CreateFullRelocationBoxCommand command) {
        FullRelocationEvent event = new FullRelocationEvent(
                null,
                command.userId(),
                command.title(),
                command.originLocation(),
                command.destinationLocation(),
                command.saleDeadline(),
                command.arrivalDeliveryDate(),
                command.takeItAllPrice(),
                command.discountRules(),
                null,
                RelocationEvent.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }
}
