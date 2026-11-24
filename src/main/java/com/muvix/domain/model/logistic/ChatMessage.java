package com.muvix.domain.model.logistic;

import java.time.Instant;

public record ChatMessage(Long id, Long orderId, Long offerId, Long templateId, Long senderId, Type type, String content,
                          Instant createdAt, Long proposalId) {
    public enum Type { TEXT, DATETIME_PROPOSAL, DATETIME_AGREEMENT, DATETIME_CONFIRMATION, DATETIME_DECLINED, PRICE_PROPOSAL, PRICE_AGREEMENT, SYSTEM }
    public ChatMessage(Long id,Long orderId,Long senderId,Type type,String content,Instant createdAt,Long proposalId){
        this(id,orderId,null,null,senderId,type,content,createdAt,proposalId);
    }
}