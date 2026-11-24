package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name="pickup_coordination_confirmations", uniqueConstraints=@UniqueConstraint(name="uk_pickup_coord_confirm_actor",columnNames={"proposal_id","user_id"}))
public class PickupCoordinationConfirmationJpaEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @Column(name="proposal_id",nullable=false)
    private Long proposalId;

    @Column(name="user_id",nullable=false)
    private Long userId;

    @Column(nullable=false)
    private boolean accepted;

    @Column(name="responded_at",nullable=false)
    private Instant respondedAt;

    public Long getId(){return id;} public void setId(Long v){id=v;}
    public Long getProposalId(){return proposalId;} public void setProposalId(Long v){proposalId=v;}
    public Long getUserId(){return userId;} public void setUserId(Long v){userId=v;}
    public boolean isAccepted(){return accepted;} public void setAccepted(boolean v){accepted=v;}
    public Instant getRespondedAt(){return respondedAt;} public void setRespondedAt(Instant v){respondedAt=v;}
}