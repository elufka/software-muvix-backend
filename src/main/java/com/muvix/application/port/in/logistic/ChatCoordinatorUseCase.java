package com.muvix.application.port.in.logistic;

import com.muvix.domain.model.logistic.*;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatCoordinatorUseCase {
    record SendMessage(Long orderId, Long senderId, String content, Long templateId) {}
    record ProposePickupTime(Long orderId, Long proposerId, Instant startTime, Instant endTime, AuthorizedPickupPerson authorizedPerson) {}
    record CreateTemplate(ChatMessageTemplate.Type type, String title, String message, Long adminId) {}
    List<ChatMessage> history(Long orderId, Long userId);
    List<ChatMessage> offerHistory(Long offerId,Long userId);
    void initializeOrderChat(Long orderId,boolean priceConversation);
    void linkOfferMessagesToOrder(Long offerId,Long orderId);
    void recordSystemMessage(Long orderId,String content);
    List<ChatMessageTemplate> templates(ChatMessageTemplate.Type type);
    List<ChatMessageTemplate> manageTemplates();
    ChatMessage send(SendMessage command);
    ChatMessage sendOfferMessage(Long offerId,Long senderId,String content,Long templateId);
    PickupCoordination proposePickupTime(ProposePickupTime command);
    Optional<PickupCoordination> currentPickupTime(Long orderId,Long userId);
    PickupCoordination agreeToPickupTime(Long orderId, Long proposalId, Long userId);
    PickupCoordination confirmPickupTime(Long orderId, Long proposalId, Long userId, boolean accepted);
    ChatMessageTemplate createTemplate(CreateTemplate command);
    ChatMessageTemplate updateTemplate(Long id, CreateTemplate command);
    void deleteTemplate(Long id);
    boolean canAccessOrder(Long orderId, Long userId);
    boolean canAccessOffer(Long offerId,Long userId);
}