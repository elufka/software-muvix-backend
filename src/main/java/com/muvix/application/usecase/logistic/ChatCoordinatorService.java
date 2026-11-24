package com.muvix.application.usecase.logistic;

import com.muvix.application.port.in.logistic.ChatCoordinatorUseCase;
import com.muvix.application.port.out.persistence.*;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.logistic.*;
import com.muvix.domain.model.transaction.Order;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.Clock;
import java.util.List;
import java.util.Objects;

@Service
public class ChatCoordinatorService implements ChatCoordinatorUseCase {
    private final ChatPersistencePort chat;
    private final OrderRepositoryPort orders;
    private final SellableItemRepositoryPort items;
    private final PickupSlotRepositoryPort slots;
    private final OfferRepositoryPort offers;
    private final Clock clock;
    private final SimpMessagingTemplate messaging;
    public ChatCoordinatorService(ChatPersistencePort chat, OrderRepositoryPort orders, SellableItemRepositoryPort items,
                                  PickupSlotRepositoryPort slots,OfferRepositoryPort offers, Clock clock, SimpMessagingTemplate messaging) {
        this.chat=chat;this.orders=orders;this.items=items;this.slots=slots;this.offers=offers;this.clock=clock;this.messaging=messaging;
    }
    @Override @Transactional(readOnly=true) public List<ChatMessage> history(Long orderId,Long userId){requireParticipant(orderId,userId);return chat.findMessages(orderId);}
    @Override @Transactional(readOnly=true) public List<ChatMessage> offerHistory(Long offerId,Long userId){requireOfferParticipant(offerId,userId);return chat.findOfferMessages(offerId);}
    @Override @Transactional public void initializeOrderChat(Long orderId,boolean priceConversation){
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        List<ChatMessageTemplate> defaults=new java.util.ArrayList<>(chat.findActiveTemplates(ChatMessageTemplate.Type.DATETIME));
        if(priceConversation)defaults.addAll(chat.findActiveTemplates(ChatMessageTemplate.Type.PRICE));
        defaults.forEach(t->chat.saveMessage(new ChatMessage(null,orderId,null,t.id(),null,ChatMessage.Type.SYSTEM,t.message(),clock.instant(),null)));
    }
    @Override @Transactional public void linkOfferMessagesToOrder(Long offerId,Long orderId){chat.linkOfferMessagesToOrder(offerId,orderId);}
    @Override @Transactional public void recordSystemMessage(Long orderId,String content){
        if(orders.findById(orderId).isEmpty())throw new DomainException("Order not found");
        broadcast(chat.saveMessage(new ChatMessage(null,orderId,null,ChatMessage.Type.SYSTEM,content,clock.instant(),null)));
    }
    @Override @Transactional(readOnly=true) public List<ChatMessageTemplate> templates(ChatMessageTemplate.Type type){return chat.findActiveTemplates(type);}
    @Override @Transactional(readOnly=true) public List<ChatMessageTemplate> manageTemplates(){return chat.findAllTemplates();}
    @Override @Transactional public ChatMessage send(SendMessage command){
        requireParticipant(command.orderId(),command.senderId());
        String content=command.content();
        if(command.templateId()!=null){
            ChatMessageTemplate template=chat.findTemplate(command.templateId()).filter(ChatMessageTemplate::active).orElseThrow(()->new DomainException("Default message not found"));
            if(template.type()!=ChatMessageTemplate.Type.DATETIME)throw new DomainException("Only date and time templates can be used in order chat");
            content=template.message();
        }
        if(content==null||content.isBlank()||content.length()>4000)throw new DomainException("Message must contain 1 to 4000 characters");
        ChatMessage saved=chat.saveMessage(new ChatMessage(null,command.orderId(),null,command.templateId(),command.senderId(),ChatMessage.Type.TEXT,content,clock.instant(),null));
        broadcast(saved); return saved;
    }
    @Override @Transactional public ChatMessage sendOfferMessage(Long offerId,Long senderId,String content,Long templateId){
        requireOfferParticipant(offerId,senderId);
        if(templateId!=null){var template=chat.findTemplate(templateId).filter(ChatMessageTemplate::active).orElseThrow(()->new DomainException("Default message not found"));if(template.type()!=ChatMessageTemplate.Type.PRICE)throw new DomainException("Only price templates can be used in offer chat");content=template.message();}
        if(content==null||content.isBlank()||content.length()>4000)throw new DomainException("Message must contain 1 to 4000 characters");
        ChatMessage m=chat.saveMessage(new ChatMessage(null,null,offerId,templateId,senderId,ChatMessage.Type.TEXT,content,clock.instant(),null));
        messaging.convertAndSend("/topic/offers/"+offerId,m);return m;
    }
    @Override @Transactional public PickupCoordination proposePickupTime(ProposePickupTime command){
        Order order=requireParticipant(command.orderId(),command.proposerId());
        if(order.getDeliveryMethod()!=Order.DeliveryMethod.PICKUP||order.getStatus()!=Order.Status.PENDING_SLOT)
            throw new DomainException("Order is not awaiting pickup coordination");
        if(command.startTime()==null||command.endTime()==null||!command.endTime().isAfter(command.startTime())
                || !command.startTime().isAfter(clock.instant()))
            throw new DomainException("Pickup time must be a valid future time range");
        if(command.authorizedPerson()==null)throw new DomainException("Authorized pickup person is required");
        chat.findCurrentCoordination(order.getId()).filter(c->c.status()!=PickupCoordination.Status.CONFIRMED&&c.status()!=PickupCoordination.Status.DECLINED)
                .ifPresent(c->chat.saveCoordination(withStatus(c,PickupCoordination.Status.SUPERSEDED)));
        PickupCoordination c=new PickupCoordination(null,order.getId(),command.proposerId(),command.startTime(),command.endTime(),command.authorizedPerson(),
                PickupCoordination.Status.PROPOSED,false,false,clock.instant());
        c=chat.saveCoordination(c);
        String text="Pickup time proposed: "+command.startTime()+" – "+command.endTime();
        broadcast(chat.saveMessage(new ChatMessage(null,order.getId(),command.proposerId(),ChatMessage.Type.DATETIME_PROPOSAL,text,clock.instant(),c.id())));
        return c;
    }
    @Override @Transactional(readOnly=true) public java.util.Optional<PickupCoordination> currentPickupTime(Long orderId,Long userId){requireParticipant(orderId,userId);return chat.findCurrentCoordination(orderId);}
    @Override @Transactional public PickupCoordination agreeToPickupTime(Long orderId,Long proposalId,Long userId){
        Order order=requireParticipant(orderId,userId); PickupCoordination c=current(orderId,proposalId);
        if(c.status()!=PickupCoordination.Status.PROPOSED)throw new DomainException("Pickup proposal is not awaiting chat agreement");
        Long buyer=order.getBuyerId(),seller=sellerId(order);
        if((Objects.equals(userId,buyer)&&c.buyerAgreedInChat())||(Objects.equals(userId,seller)&&c.sellerAgreedInChat()))throw new DomainException("You already agreed to this pickup time");
        boolean buyerAgreed=c.buyerAgreedInChat()||Objects.equals(userId,buyer),sellerAgreed=c.sellerAgreedInChat()||Objects.equals(userId,seller);
        var state=buyerAgreed&&sellerAgreed?PickupCoordination.Status.AWAITING_CONFIRMATION:PickupCoordination.Status.PROPOSED;
        c=new PickupCoordination(c.id(),c.orderId(),c.proposerId(),c.startTime(),c.endTime(),c.authorizedPerson(),state,buyerAgreed,sellerAgreed,c.createdAt());
        c=chat.saveCoordination(c);
        String agreementText=state==PickupCoordination.Status.AWAITING_CONFIRMATION
                ? "Both participants agreed in chat. Confirm the time in the popup to finalize."
                : "Agreed to the proposed pickup time. Waiting for the other participant.";
        broadcast(chat.saveMessage(new ChatMessage(null,orderId,userId,ChatMessage.Type.DATETIME_AGREEMENT,agreementText,clock.instant(),proposalId)));
        return c;
    }
    @Override @Transactional public PickupCoordination confirmPickupTime(Long orderId,Long proposalId,Long userId,boolean accepted){
        Order order=requireParticipant(orderId,userId); PickupCoordination c=current(orderId,proposalId);
        if(c.status()!=PickupCoordination.Status.AWAITING_CONFIRMATION||!c.buyerAgreedInChat()||!c.sellerAgreedInChat())
            throw new DomainException("Both participants must agree in chat before confirming");
        Long buyer=order.getBuyerId(),seller=sellerId(order);
        if(chat.hasConfirmation(proposalId,userId))throw new DomainException("You already confirmed this pickup proposal");
        chat.saveConfirmation(proposalId,userId,accepted);
        if(!accepted){
            c=withStatus(c,PickupCoordination.Status.DECLINED);chat.saveCoordination(c);
            String name=Objects.equals(userId,buyer)?order.getBuyerName():order.getSellerName();
            broadcast(chat.saveMessage(new ChatMessage(null,orderId,null,ChatMessage.Type.DATETIME_DECLINED,name+" did not agree to the pickup time. They may propose a new time.",clock.instant(),proposalId)));
            return c;
        }
        broadcast(chat.saveMessage(new ChatMessage(null,orderId,userId,ChatMessage.Type.DATETIME_CONFIRMATION,"Confirmed the pickup time.",clock.instant(),proposalId)));
        if(chat.confirmationAccepted(proposalId,buyer)&&chat.confirmationAccepted(proposalId,seller)){
            PickupSlot slot=PickupSlot.requestSlot(orderId,c.startTime(),c.endTime());slot.confirm(buyer,buyer,seller);slot.confirm(seller,buyer,seller);slot=slots.save(slot);
            order.setAuthorizedPickupPerson(c.authorizedPerson());order.attachPickupSlotProposal(slot.getId());order.confirmSlot(slot.getId());orders.save(order);
            c=withStatus(c,PickupCoordination.Status.CONFIRMED);chat.saveCoordination(c);
            broadcast(chat.saveMessage(new ChatMessage(null,orderId,null,ChatMessage.Type.SYSTEM,"Pickup time confirmed. Checkout is now available.",clock.instant(),proposalId)));
        }
        return c;
    }
    @Override @Transactional public ChatMessageTemplate createTemplate(CreateTemplate c){return saveTemplate(null,c);}
    @Override @Transactional public ChatMessageTemplate updateTemplate(Long id,CreateTemplate c){chat.findTemplate(id).orElseThrow(()->new DomainException("Default message not found"));return saveTemplate(id,c);}
    private ChatMessageTemplate saveTemplate(Long id,CreateTemplate c){
        if(c.type()==null||c.title()==null||c.title().isBlank()||c.title().length()>160||c.message()==null||c.message().isBlank()||c.message().length()>2000)throw new DomainException("Template type, title and message are required");
        var old=id==null?null:chat.findTemplate(id).orElseThrow(()->new DomainException("Default message not found"));
        return chat.saveTemplate(new ChatMessageTemplate(id,c.type(),c.title().trim(),c.message().trim(),old==null?c.adminId():old.createdBy(),old==null?clock.instant():old.createdAt(),true));
    }
    @Override @Transactional public void deleteTemplate(Long id){
        var old=chat.findTemplate(id).orElseThrow(()->new DomainException("Default message not found"));
        chat.saveTemplate(new ChatMessageTemplate(old.id(),old.type(),old.title(),old.message(),old.createdBy(),old.createdAt(),false));
    }
    @Override @Transactional(readOnly=true) public boolean canAccessOrder(Long orderId,Long userId){try{requireParticipant(orderId,userId);return true;}catch(DomainException e){return false;}}
    @Override @Transactional(readOnly=true) public boolean canAccessOffer(Long offerId,Long userId){try{requireOfferParticipant(offerId,userId);return true;}catch(DomainException e){return false;}}
    private void requireOfferParticipant(Long offerId,Long userId){var offer=offers.findById(offerId).orElseThrow(()->new DomainException("Offer not found"));var item=items.findById(offer.getItemId()).orElseThrow(()->new DomainException("Sellable item not found"));if(!Objects.equals(offer.getBuyerId(),userId)&&!Objects.equals(item.getSellerId(),userId))throw new DomainException("Only offer participants can access its chat");}
    private PickupCoordination current(Long orderId,Long proposalId){return chat.findCoordinationForUpdate(proposalId).filter(c->c.orderId().equals(orderId)).orElseThrow(()->new DomainException("Pickup proposal not found"));}
    private PickupCoordination withStatus(PickupCoordination c,PickupCoordination.Status status){return new PickupCoordination(c.id(),c.orderId(),c.proposerId(),c.startTime(),c.endTime(),c.authorizedPerson(),status,c.buyerAgreedInChat(),c.sellerAgreedInChat(),c.createdAt());}
    private Order requireParticipant(Long orderId,Long userId){
        Order order=orders.findById(orderId).orElseThrow(()->new DomainException("Order not found"));
        if(!Objects.equals(order.getBuyerId(),userId)&&!Objects.equals(sellerId(order),userId))throw new DomainException("Only order participants can access its chat");return order;
    }
    private Long sellerId(Order order){List<Long> sellers=order.getItemIds().stream().map(id->items.findById(id).orElseThrow(()->new DomainException("Order item not found"))).map(SellableItem::getSellerId).distinct().toList();if(sellers.size()!=1)throw new DomainException("Chat supports orders with one seller");return sellers.get(0);}
    private void broadcast(ChatMessage message){messaging.convertAndSend("/topic/orders/"+message.orderId(),message);}
}