package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chat_messages", indexes = {@Index(name = "idx_chat_order_created", columnList = "order_id,created_at"),@Index(name="idx_chat_offer_created",columnList="offer_id,created_at")})
public class ChatMessageJpaEntity {
    public enum Type { TEXT, DATETIME_PROPOSAL, DATETIME_AGREEMENT, DATETIME_CONFIRMATION, DATETIME_DECLINED, PRICE_PROPOSAL, PRICE_AGREEMENT, SYSTEM }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id")
    private Long orderId;

    @Column(name="offer_id")
    private Long offerId;

    @Column(name="template_id")
    private Long templateId;

    @Column(name="sender_id")
    private Long senderId;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false, length=40)
    private Type type;

    @Column(nullable=false, length=4000)
    private String content;

    @Column(name="created_at", nullable=false)
    private Instant createdAt;

    @Column(name="proposal_id")
    private Long proposalId;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;}
    public Long getOfferId(){return offerId;} public void setOfferId(Long v){offerId=v;}
    public Long getTemplateId(){return templateId;} public void setTemplateId(Long v){templateId=v;}
    public Long getSenderId(){return senderId;} public void setSenderId(Long v){senderId=v;}
    public Type getType(){return type;} public void setType(Type v){type=v;}
    public String getContent(){return content;} public void setContent(String v){content=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public Long getProposalId(){return proposalId;} public void setProposalId(Long v){proposalId=v;}
}