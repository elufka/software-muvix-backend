package com.muvix.application.usecase.transaction;
import com.muvix.application.port.in.transaction.ManageOfferUseCase;
import com.muvix.application.port.out.persistence.OfferRepositoryPort;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.application.port.out.persistence.ChatPersistencePort;
import com.muvix.domain.model.logistic.ChatMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.negotiation.Offer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
@Service
public class ManageOfferService implements ManageOfferUseCase {
    private final OfferRepositoryPort offers;
    private final SellableItemRepositoryPort items;
    private final Clock clock;
    private final ChatPersistencePort chat;
    private final SimpMessagingTemplate messaging;

    @Autowired
    public ManageOfferService(OfferRepositoryPort offers, SellableItemRepositoryPort items, Clock clock,ChatPersistencePort chat,SimpMessagingTemplate messaging) {
        this.offers=offers;
        this.items=items;
        this.clock=clock;
        this.chat=chat;
        this.messaging=messaging;
    }

    public ManageOfferService(OfferRepositoryPort offers,SellableItemRepositoryPort items,Clock clock){
        this.offers=offers;
        this.items=items;
        this.clock=clock;
        this.chat=null;
        this.messaging=null;
    }

    @Override
    @Transactional
    public Offer create(CreateOfferCommand command){
        SellableItem item=items.findById(command.itemId()).orElseThrow(()->new DomainException("Sellable item not found"));
        Offer saved=offers.save(Offer.create(item,command.buyerId(),command.amount(),clock.instant()));

        if(chat!=null)chat.
                findActiveTemplates(com.muvix.domain.model.logistic.ChatMessageTemplate.Type.PRICE).
                forEach(t->{
                    ChatMessage m=chat.saveMessage(new ChatMessage(
                        null,
                        null,
                        saved.getId(),
                        t.id(),
                        null,
                        ChatMessage.Type.SYSTEM,
                        t.message(),
                        clock.instant(),
                        null
                    )
                    );

            if(messaging!=null)messaging.convertAndSend("/topic/offers/"+saved.getId(),m);
        });

        post(saved.getId(),saved.getBuyerId(),ChatMessage.Type.PRICE_PROPOSAL,"Buyer proposed "+saved.getAmount());return saved;
    }

    @Override
    @Transactional(readOnly=true)
    public Offer get(Long offerId,Long actorId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));

        if(!offer.getBuyerId().equals(actorId)&&!item.getSellerId().equals(actorId))throw new DomainException("Only offer participants can read it");

        return offer;
    }

    @Override
    @Transactional
    public Offer reject(Long offerId,Long sellerId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));

        if(!item.getSellerId().equals(sellerId)) throw new DomainException("Only the seller can reject this offer");
        offer.reject();

        return offers.save(offer);
    }

    @Override
    @Transactional
    public Offer counter(Long offerId,Long sellerId,java.math.BigDecimal amount){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));

        if(!item.getSellerId().equals(sellerId))throw new DomainException("Only the seller can counter an offer");

        if(amount==null||amount.compareTo(item.getPrice())>0)throw new DomainException("Counteroffer cannot exceed the listing price");

        offer.counter(amount);
        Offer saved=offers.save(offer);
        post(offerId,sellerId,ChatMessage.Type.PRICE_PROPOSAL,"Seller counteroffered "+amount);

        return saved;
    }

    @Override
    @Transactional
    public Offer acceptInitial(Long offerId,Long sellerId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));

        if(!item.getSellerId().equals(sellerId))throw new DomainException("Only the seller can accept an offer");

        offer.accept();
        Offer saved=offers.save(offer);

        post(offerId,sellerId,ChatMessage.Type.PRICE_AGREEMENT,"Seller accepted "+offer.getAmount());
        return saved;
    }

    @Override
    @Transactional
    public Offer acceptCounter(Long offerId,Long buyerId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));
        SellableItem item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));

        if(!offer.getBuyerId().equals(buyerId))throw new DomainException("Only the buyer can accept a counteroffer");

        if(offer.getCounterAmount()==null||offer.getCounterAmount().compareTo(item.getPrice())>0)throw new DomainException("Invalid seller counteroffer");

        offer.acceptCounter(buyerId);
        Offer saved=offers.save(offer);

        post(offerId,buyerId,ChatMessage.Type.PRICE_AGREEMENT,"Buyer accepted the seller's counteroffer of "+offer.getAgreedAmount());
        return saved;
    }

    @Override
    @Transactional
    public Offer rejectCounter(Long offerId,Long buyerId){
        Offer offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));

        if(!offer.getBuyerId().equals(buyerId))throw new DomainException("Only the buyer can reject a counteroffer");

        if(offer.getStatus()!=Offer.Status.COUNTERED)throw new DomainException("There is no seller counteroffer to reject");
        offer.reject();

        return offers.save(offer);
    }

    private void post(Long offerId,Long senderId,ChatMessage.Type type,String text) {
        if(chat!=null){
            ChatMessage message=chat.
                    saveMessage(
                            new ChatMessage(
                                    null,
                                    null,
                                    offerId,
                                    null,
                                    senderId,
                                    type,text,
                                    clock.instant(),
                                    null
                            )
                    );
            if(messaging!=null) messaging.convertAndSend("/topic/offers/"+offerId,message);
        }
    }
}