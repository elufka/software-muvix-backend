package com.muvix.application.usecase.event;
import com.muvix.application.port.in.event.ManageRelocationEventUseCase;
import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class ManageRelocationEventService implements ManageRelocationEventUseCase {
    private final RelocationEventRepositoryPort events;
    public ManageRelocationEventService(RelocationEventRepositoryPort events){this.events=events;}
    @Override @Transactional(readOnly=true) public RelocationEvent get(Long id,Long userId){
        RelocationEvent event=find(id); if(!event.isMember(userId))throw new DomainException("You cannot access this event"); return event;
    }
    @Override @Transactional public RelocationEvent replace(ReplaceCommand c){
        RelocationEvent old=find(c.eventId()); if(!old.getUserId().equals(c.userId()))throw new DomainException("Only the event owner can replace it");
        if(old.getStatus()==RelocationEvent.Status.CANCELLED||old.getStatus()==RelocationEvent.Status.COMPLETED)throw new DomainException("Closed events cannot be edited");
        RelocationEvent updated;
        if(old instanceof SaleEvent) updated=new SaleEvent(old.getId(),old.getUserId(),c.title(),c.originLocation(),c.saleDeadline(),c.takeItAllPrice(),c.discountRules(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else if(old instanceof PurchaseEvent) updated=new PurchaseEvent(old.getId(),old.getUserId(),c.title(),c.destinationLocation(),c.arrivalDeliveryDate(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else if(old instanceof FullRelocationEvent) updated=new FullRelocationEvent(old.getId(),old.getUserId(),c.title(),c.originLocation(),c.destinationLocation(),c.saleDeadline(),c.arrivalDeliveryDate(),c.takeItAllPrice(),c.discountRules(),old.getUniqueShareToken(),old.getStatus(),old.getSharedUserIds());
        else throw new DomainException("Unsupported event type");
        return events.save(updated);
    }
    @Override @Transactional public void cancel(Long id,Long userId){RelocationEvent event=find(id);if(!event.getUserId().equals(userId))throw new DomainException("Only the event owner can cancel it");event.cancel();events.save(event);}
    private RelocationEvent find(Long id){return events.findById(id).orElseThrow(()->new DomainException("Event not found"));}
}
