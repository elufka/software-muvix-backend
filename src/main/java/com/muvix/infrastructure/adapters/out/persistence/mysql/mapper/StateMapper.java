package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;

//Mapeador de estado de enum de SelleableItem atributo State
public class StateMapper {
    private StateMapper() {}

    static SellableItem.State toDomain(SellableItemJpaEntity.StateJpa s) {
        return s == null ? SellableItem.State.AVAILABLE : SellableItem.State.valueOf(s.name());
    }

    static SellableItemJpaEntity.StateJpa toJpa(SellableItem.State s) {
        return s == null ? SellableItemJpaEntity.StateJpa.AVAILABLE
                : SellableItemJpaEntity.StateJpa.valueOf(s.name());
    }
}