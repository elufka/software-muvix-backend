package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.Batch;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.BatchJpaEntity;

import java.util.Set;

public class BatchPersistenceMapper {
    private BatchPersistenceMapper() {}

    public static Batch toDomain(BatchJpaEntity e) {
        return new Batch(
                e.getId(),
                e.getSellerId(),
                e.getTitle(),
                e.getDescription(),
                e.getPrice(),
                StateMapper.toDomain(e.getState()),
                e.getCategory(),
                Set.copyOf(e.getProductIds())
        );
    }

    public static BatchJpaEntity toEntity(Batch b) {
        BatchJpaEntity e = new BatchJpaEntity();
        updateEntity(b, e);
        return e;
    }

    public static void updateEntity(Batch b, BatchJpaEntity e) {
        e.setSellerId(b.getSellerId());
        e.setTitle(b.getTitle());
        e.setDescription(b.getDescription());
        e.setPrice(b.getPrice());
        e.setCategory(b.getCategory());
        e.setState(StateMapper.toJpa(b.getState()));
        e.getProductIds().clear();
        e.getProductIds().addAll(b.getProductIds());
    }
}
