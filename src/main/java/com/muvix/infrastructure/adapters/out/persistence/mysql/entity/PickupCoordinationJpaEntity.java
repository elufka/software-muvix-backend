package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="pickup_coordinations", indexes=@Index(name="idx_pickup_coord_order",columnList="order_id,id"))
public class PickupCoordinationJpaEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="order_id",nullable=false)
    private Long orderId;

    @Column(name="proposer_id",nullable=false)
    private Long proposerId;

    @Column(name="start_time",nullable=false)
    private Instant startTime;

    @Column(name="end_time",nullable=false)
    private Instant endTime;

    @Embedded
    private AuthorizedPickupPersonEmbeddable authorizedPerson;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false,length=30)
    private com.muvix.domain.model.logistic.PickupCoordination.Status status;

    @Column(name="buyer_agreed_in_chat",nullable=false)
    private boolean buyerAgreedInChat;

    @Column(name="seller_agreed_in_chat",nullable=false)
    private boolean sellerAgreedInChat;

    @Column(name="created_at",nullable=false)
    private Instant createdAt;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getOrderId(){return orderId;} public void setOrderId(Long v){orderId=v;}
    public Long getProposerId(){return proposerId;} public void setProposerId(Long v){proposerId=v;}
    public Instant getStartTime(){return startTime;} public void setStartTime(Instant v){startTime=v;}
    public Instant getEndTime(){return endTime;} public void setEndTime(Instant v){endTime=v;}
    public AuthorizedPickupPersonEmbeddable getAuthorizedPerson(){return authorizedPerson;} public void setAuthorizedPerson(AuthorizedPickupPersonEmbeddable v){authorizedPerson=v;}
    public com.muvix.domain.model.logistic.PickupCoordination.Status getStatus(){return status;} public void setStatus(com.muvix.domain.model.logistic.PickupCoordination.Status v){status=v;}
    public boolean isBuyerAgreedInChat(){return buyerAgreedInChat;} public void setBuyerAgreedInChat(boolean v){buyerAgreedInChat=v;}
    public boolean isSellerAgreedInChat(){return sellerAgreedInChat;} public void setSellerAgreedInChat(boolean v){sellerAgreedInChat=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
}