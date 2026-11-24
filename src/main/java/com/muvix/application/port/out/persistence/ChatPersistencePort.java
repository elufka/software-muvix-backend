package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.logistic.ChatMessage;
import com.muvix.domain.model.logistic.ChatMessageTemplate;
import com.muvix.domain.model.logistic.PickupCoordination;
import java.util.List;
import java.util.Optional;

public interface ChatPersistencePort {
    ChatMessage saveMessage(ChatMessage message);
    List<ChatMessage> findMessages(Long orderId);
    List<ChatMessage> findOfferMessages(Long offerId);
    void linkOfferMessagesToOrder(Long offerId,Long orderId);
    ChatMessageTemplate saveTemplate(ChatMessageTemplate template);
    Optional<ChatMessageTemplate> findTemplate(Long id);
    List<ChatMessageTemplate> findActiveTemplates(ChatMessageTemplate.Type type);
    List<ChatMessageTemplate> findAllTemplates();
    void deleteTemplate(Long id);
    PickupCoordination saveCoordination(PickupCoordination coordination);
    Optional<PickupCoordination> findCoordination(Long id);
    Optional<PickupCoordination> findCoordinationForUpdate(Long id);
    Optional<PickupCoordination> findCurrentCoordination(Long orderId);
    void saveConfirmation(Long proposalId, Long userId, boolean accepted);
    boolean hasConfirmation(Long proposalId, Long userId);
    boolean confirmationAccepted(Long proposalId, Long userId);
}