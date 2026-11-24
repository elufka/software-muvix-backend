package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.ChatPersistencePort;
import com.muvix.domain.model.logistic.*;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.*;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class ChatMySQLAdapter implements ChatPersistencePort {
    private final ChatMessageJpaRepository messages;
    private final ChatMessageTemplateJpaRepository templates;
    private final PickupCoordinationJpaRepository coordinations;
    private final PickupCoordinationConfirmationJpaRepository confirmations;

    public ChatMySQLAdapter(ChatMessageJpaRepository messages, ChatMessageTemplateJpaRepository templates,
                            PickupCoordinationJpaRepository coordinations,
                            PickupCoordinationConfirmationJpaRepository confirmations) {
        this.messages=messages; this.templates=templates; this.coordinations=coordinations; this.confirmations=confirmations;
    }

    @Override
    public ChatMessage saveMessage(ChatMessage m) {
        ChatMessageJpaEntity e=new ChatMessageJpaEntity();

        e.setId(m.id());
        e.setOrderId(m.orderId());
        e.setOfferId(m.offerId());
        e.setTemplateId(m.templateId());
        e.setSenderId(m.senderId());
        e.setType(ChatMessageJpaEntity.Type.valueOf(m.type().name()));
        e.setContent(m.content()); e.setCreatedAt(m.createdAt());
        e.setProposalId(m.proposalId());

        return toDomain(messages.save(e));
    }

    @Override
    @Transactional(readOnly=true)
    public List<ChatMessage> findMessages(Long orderId) {
        return messages.findByOrderIdOrderByCreatedAtAscIdAsc(orderId).stream().map(ChatMySQLAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly=true)
    public List<ChatMessage> findOfferMessages(Long offerId){
        return messages.findByOfferIdOrderByCreatedAtAscIdAsc(offerId).stream().map(ChatMySQLAdapter::toDomain).toList();
    }

    @Override
    public void linkOfferMessagesToOrder(Long offerId,Long orderId){
        messages.linkOfferMessages(offerId,orderId);
    }

    @Override
    public ChatMessageTemplate saveTemplate(ChatMessageTemplate t) {
        ChatMessageTemplateJpaEntity e=t.id()==null?new ChatMessageTemplateJpaEntity():templates.findById(t.id()).orElseGet(ChatMessageTemplateJpaEntity::new);

        e.setId(t.id());
        e.setType(ChatMessageTemplateJpaEntity.Type.valueOf(t.type().name()));
        e.setTitle(t.title()); e.setMessage(t.message());
        e.setCreatedBy(t.createdBy());
        e.setCreatedAt(t.createdAt());
        e.setActive(t.active());

        return toDomain(templates.save(e));
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<ChatMessageTemplate> findTemplate(Long id){
        return templates.findById(id).map(ChatMySQLAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly=true)
    public List<ChatMessageTemplate> findActiveTemplates(ChatMessageTemplate.Type type){
        return templates.findByTypeAndActiveTrueOrderByIdAsc(ChatMessageTemplateJpaEntity.Type.valueOf(type.name())).stream().map(ChatMySQLAdapter::toDomain).toList();
    }

    @Override
    @Transactional(readOnly=true)
    public List<ChatMessageTemplate> findAllTemplates(){
        return templates.findAll().stream().map(ChatMySQLAdapter::toDomain).toList();
    }

    @Override
    public void deleteTemplate(Long id){
        templates.deleteById(id);
    }

    @Override
    public PickupCoordination saveCoordination(PickupCoordination c) {
        PickupCoordinationJpaEntity e=c.id()==null?new PickupCoordinationJpaEntity():coordinations.findById(c.id()).orElseGet(PickupCoordinationJpaEntity::new);

        e.setId(c.id()); e.setOrderId(c.orderId()); e.setProposerId(c.proposerId()); e.setStartTime(c.startTime()); e.setEndTime(c.endTime());
        e.setStatus(c.status()); e.setBuyerAgreedInChat(c.buyerAgreedInChat()); e.setSellerAgreedInChat(c.sellerAgreedInChat()); e.setCreatedAt(c.createdAt());

        if(c.authorizedPerson()!=null){
            AuthorizedPickupPersonEmbeddable p=new AuthorizedPickupPersonEmbeddable();

            p.setFullName(c.authorizedPerson().fullName());
            p.setDocumentId(c.authorizedPerson().documentId());
            p.setPhone(c.authorizedPerson().phone());

            e.setAuthorizedPerson(p);
        }

        return toDomain(coordinations.save(e));
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<PickupCoordination> findCoordination(Long id){
        return coordinations.findById(id).map(ChatMySQLAdapter::toDomain);
    }

    @Override
    public Optional<PickupCoordination> findCoordinationForUpdate(Long id){
        return coordinations.lockById(id).map(ChatMySQLAdapter::toDomain);
    }

    @Override
    @Transactional(readOnly=true)
    public Optional<PickupCoordination> findCurrentCoordination(Long orderId){
        return coordinations.findFirstByOrderIdOrderByIdDesc(orderId)
                .filter(c->c.getStatus()!=PickupCoordination.Status.SUPERSEDED)
                .map(ChatMySQLAdapter::toDomain);
    }

    @Override
    public void saveConfirmation(Long proposalId,Long userId,boolean accepted){
        var e=confirmations.findByProposalIdAndUserId(proposalId,userId).orElseGet(PickupCoordinationConfirmationJpaEntity::new);

        e.setProposalId(proposalId);
        e.setUserId(userId);
        e.setAccepted(accepted);
        e.setRespondedAt(Instant.now());

        confirmations.save(e);
    }

    @Override
    @Transactional(readOnly=true)
    public boolean hasConfirmation(Long proposalId,Long userId){
        return confirmations.findByProposalIdAndUserId(proposalId,userId).isPresent();
    }

    @Override
    @Transactional(readOnly=true)
    public boolean confirmationAccepted(Long proposalId,Long userId){
        return confirmations.findByProposalIdAndUserId(proposalId,userId)
                .map(PickupCoordinationConfirmationJpaEntity::isAccepted)
                .orElse(false);
    }

    private static ChatMessage toDomain(ChatMessageJpaEntity e){
        return new ChatMessage(
                e.getId(),
                e.getOrderId(),
                e.getOfferId(),
                e.getTemplateId(),
                e.getSenderId(),
                ChatMessage.Type.valueOf(e.getType().name()),
                e.getContent(),
                e.getCreatedAt(),
                e.getProposalId()
        );
    }

    private static ChatMessageTemplate toDomain(ChatMessageTemplateJpaEntity e){
        return new ChatMessageTemplate(
                e.getId(),
                ChatMessageTemplate.Type.valueOf(e.getType().name()),
                e.getTitle(),
                e.getMessage(),
                e.getCreatedBy(),
                e.getCreatedAt(),
                e.isActive()
        );
    }

    private static PickupCoordination toDomain(PickupCoordinationJpaEntity e){
        var p=e.getAuthorizedPerson();
        var person=p==null?null:new AuthorizedPickupPerson(p.getFullName(),p.getDocumentId(),p.getPhone());
        return new PickupCoordination(
                e.getId(),
                e.getOrderId(),
                e.getProposerId(),
                e.getStartTime(),
                e.getEndTime(),
                person,
                e.getStatus(),
                e.isBuyerAgreedInChat(),
                e.isSellerAgreedInChat(),
                e.getCreatedAt()
        );
    }
}