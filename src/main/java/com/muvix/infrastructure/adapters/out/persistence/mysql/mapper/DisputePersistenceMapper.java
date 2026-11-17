package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.transaction.Dispute;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.DisputeJpaEntity;

public final class DisputePersistenceMapper {

    private DisputePersistenceMapper() {}

    public static Dispute toDomain(DisputeJpaEntity entity) {
        if (entity == null) return null;
        return Dispute.restore(
                entity.getId(),
                entity.getOrderId(),
                entity.getBuyerId(),
                entity.getReason(),
                entity.getEvidenceImages(),
                Dispute.Status.valueOf(entity.getStatus().name()),
                entity.getCreatedAt(), entity.getEvidenceRequestedAt(), entity.getAdditionalEvidenceRounds()
        );
    }

    public static DisputeJpaEntity toEntity(Dispute dispute) {
        if (dispute == null) return null;
        DisputeJpaEntity entity = new DisputeJpaEntity();
        entity.setId(dispute.getId());
        entity.setOrderId(dispute.getOrderId());
        entity.setBuyerId(dispute.getBuyerId());
        entity.setReason(dispute.getReason());
        entity.setEvidenceImages(new java.util.ArrayList<>(dispute.getEvidenceImages()));
        entity.setStatus(DisputeJpaEntity.StatusJpa.valueOf(dispute.getStatus().name()));
        entity.setCreatedAt(dispute.getCreatedAt());
        entity.setEvidenceRequestedAt(dispute.getEvidenceRequestedAt());
        entity.setAdditionalEvidenceRounds(dispute.getAdditionalEvidenceRounds());
        return entity;
    }
}
