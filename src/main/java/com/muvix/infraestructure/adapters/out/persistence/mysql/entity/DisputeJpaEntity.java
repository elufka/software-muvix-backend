package com.muvix.infraestructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "disputes")
public class DisputeJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String reason;

    @ElementCollection
    @CollectionTable(name = "dispute_evidence", joinColumns = @JoinColumn(name = "dispute_id"))
    @Column(name = "image_url")
    private List<String> evidenceImages = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusJpa status;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    public enum StatusJpa { OPENED, UNDER_REVIEW, RESOLVED_REFUND, RESOLVED_REJECTED }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
    public List<String> getEvidenceImages() { return evidenceImages; }
    public void setEvidenceImages(List<String> evidenceImages) { this.evidenceImages = evidenceImages; }
    public StatusJpa getStatus() { return status; }
    public void setStatus(StatusJpa status) { this.status = status; }
    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
