package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.BatchJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public class BatchPersistenceMapper {
    private BatchPersistenceMapper() {}

    public static Batch toDomain(BatchJpaEntity e) {
        return Batch.reconstitute(
                e.getId(), e.getSellerId(), e.getTitle(), e.getDescription(),
                e.getPrice(), StateMapper.toDomain(e.getState()),
                Set.copyOf(e.getProductIds()));
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
        e.setState(StateMapper.toJpa(b.getState()));
        e.getProductIds().clear();
        e.getProductIds().addAll(b.getProductIds());
    }
}
