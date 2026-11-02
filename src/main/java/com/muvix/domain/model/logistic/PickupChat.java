package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class PickupChat {

    public enum PredefinedMessageType {
        HEAVY_LIFTING_NEEDED("Notice: Items require heavy lifting / two people."),
        PARKING_AVAILABLE("Notice: Parking is available at pickup location."),
        STAIRS_ONLY("Notice: Building has stairs only (no elevator)."),
        CONFIRM_TIME_SLOT("Confirmation request for pickup time slot.");

        private final String templateText;
        PredefinedMessageType(String templateText) { this.templateText = templateText; }
        public String getTemplateText() { return templateText; }
    }

    public record ChatMessage(Long senderId, String text, Instant sentAt) {}

    private final Long orderId;
    private final List<ChatMessage> messages = new ArrayList<>();

    public PickupChat(Long orderId) {
        if (orderId == null) throw new DomainException("Order ID is required for pickup chat");
        this.orderId = orderId;
    }

    public void sendPredefinedMessage(Long senderId, PredefinedMessageType messageType) {
        messages.add(new ChatMessage(senderId, messageType.getTemplateText(), Instant.now()));
    }

    public Long getOrderId() { return orderId; }
    public List<ChatMessage> getMessages() { return List.copyOf(messages); }
}