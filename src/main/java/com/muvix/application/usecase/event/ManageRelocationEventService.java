package com.muvix.application.usecase.event;
import com.muvix.application.port.in.event.ManageRelocationEventUseCase;
import com.muvix.application.port.out.persistence.BoxRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ManageRelocationEventService implements ManageRelocationEventUseCase {
    private final BoxRepositoryPort events;
    public ManageRelocationEventService(BoxRepositoryPort events){this.events=events;}
    @Override @Transactional(readOnly=true) public Box get(Long id,Long userId){
        Box event=find(id); if(!event.isMember(userId))throw new DomainException("You cannot access this event"); return event;
    }
    @Override @Transactional public Box replace(ReplaceCommand c){
        Box old=find(c.eventId()); if(!old.getUserId().equals(c.userId()))throw new DomainException("Only the event owner can replace it");
        if(old.getStatus()==Box.Status.CANCELLED||old.getStatus()==Box.Status.COMPLETED)throw new DomainException("Closed events cannot be edited");
        Box updated;
        if(old instanceof SaleBox) updated=new SaleBox(old.getId(),old.getUserId(),c.title(),c.originLocation(),c.saleDeadline(),c.takeItAllPrice(),c.discountRules(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else if(old instanceof PurchaseBox) updated=new PurchaseBox(old.getId(),old.getUserId(),c.title(),c.destinationLocation(),c.arrivalDeliveryDate(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else if(old instanceof FullRelocationBox) updated=new FullRelocationBox(old.getId(),old.getUserId(),c.title(),c.originLocation(),c.destinationLocation(),c.saleDeadline(),c.arrivalDeliveryDate(),c.takeItAllPrice(),c.discountRules(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else throw new DomainException("Unsupported event type");
        return events.save(updated);
    }
    @Override @Transactional public void cancel(Long id,Long userId){Box event=find(id);if(!event.getUserId().equals(userId))throw new DomainException("Only the event owner can cancel it");event.cancel();events.save(event);}
    private Box find(Long id){return events.findById(id).orElseThrow(()->new DomainException("Event not found"));}
}
