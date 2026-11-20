package com.muvix.application.usecase.event;

import com.muvix.application.port.in.event.CreateRelocationEventUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.management.FullRelocationBox;
import com.muvix.domain.model.management.PurchaseBox;
import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.management.SaleBox;
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
    public Box createSaleBox(CreateSaleBoxCommand command) {
        SaleBox event = new SaleBox(
                null,
                command.userId(),
                command.title(),
                command.originLocation(),
                command.saleDeadline(),
                command.takeItAllPrice(),
                command.discountRules(),
                null,
                Box.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Box createPurchaseBox(CreatePurchaseBoxCommand command) {
        PurchaseBox event = new PurchaseBox(
                null,
                command.userId(),
                command.title(),
                command.destinationLocation(),
                command.arrivalDeliveryDate(),
                null,
                Box.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }

    @Override
    @Transactional
    public Box createFullRelocationBox(CreateFullRelocationBoxCommand command) {
        FullRelocationBox event = new FullRelocationBox(
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
                Box.Status.DRAFT,
                null
        );
        return eventRepository.save(event);
    }
}
