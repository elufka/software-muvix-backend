package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.BatchJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.ProductJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.BatchPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.ProductPersistenceMapper;

public final class SellableItemPersistenceMapper {
    private SellableItemPersistenceMapper() {}

    // Lo mappeamos a su respectivo mapeador
    public static SellableItem toDomain(SellableItemJpaEntity entity) {
        return switch (entity) {
            case null -> null;
            case ProductJpaEntity p -> ProductPersistenceMapper.toDomain(p);
            case BatchJpaEntity b -> BatchPersistenceMapper.toDomain(b);
            default -> throw new IllegalStateException("Tipo de entidad desconocido: " + entity.getClass());
        };
    }
}