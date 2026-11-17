package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
@Entity @Table(name = "offers")
public class OfferJpaEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(name="item_id", nullable=false) private Long itemId;
    @Column(name="buyer_id", nullable=false) private Long buyerId;
    @Column(nullable=false, precision=12, scale=2) private BigDecimal amount;
    @Column(name="created_at", nullable=false) private Instant createdAt;
    @Enumerated(EnumType.STRING) @Column(nullable=false) private com.muvix.domain.model.negotiation.Offer.Status status;
    public Long getId(){return id;} public void setId(Long id){this.id=id;}
    public Long getItemId(){return itemId;} public void setItemId(Long v){itemId=v;}
    public Long getBuyerId(){return buyerId;} public void setBuyerId(Long v){buyerId=v;}
    public BigDecimal getAmount(){return amount;} public void setAmount(BigDecimal v){amount=v;}
    public Instant getCreatedAt(){return createdAt;} public void setCreatedAt(Instant v){createdAt=v;}
    public com.muvix.domain.model.negotiation.Offer.Status getStatus(){return status;} public void setStatus(com.muvix.domain.model.negotiation.Offer.Status v){status=v;}
}
