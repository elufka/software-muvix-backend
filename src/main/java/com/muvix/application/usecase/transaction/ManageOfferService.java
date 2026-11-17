package com.muvix.application.usecase.transaction;
import com.muvix.application.port.in.transaction.ManageOfferUseCase;
import com.muvix.application.port.out.persistence.OfferRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.negotiation.Offer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
@Service
public class ManageOfferService implements ManageOfferUseCase {
    private final OfferRepositoryPort offers; private final SellableItemRepositoryPort items; private final Clock clock;
    public ManageOfferService(OfferRepositoryPort offers, SellableItemRepositoryPort items, Clock clock){this.offers=offers;this.items=items;this.clock=clock;}
    @Override @Transactional public Offer create(CreateOfferCommand command){
        SellableItem item=items.findById(command.itemId()).orElseThrow(()->new DomainException("Sellable item not found"));
        return offers.save(Offer.create(item,command.buyerId(),command.amount(),clock.instant()));
    }
    @Override @Transactional(readOnly=true) public Offer get(Long offerId,Long actorId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));
        if(!offer.getBuyerId().equals(actorId)&&!item.getSellerId().equals(actorId))throw new DomainException("Only offer participants can read it");
        return offer;
    }
    @Override @Transactional public Offer reject(Long offerId,Long sellerId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));
        if(!item.getSellerId().equals(sellerId))throw new DomainException("Only the seller can reject this offer");
        offer.reject(); return offers.save(offer);
    }
}
