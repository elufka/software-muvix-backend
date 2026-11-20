package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.StateJpa;

public class StateMapper {
    private StateMapper() {}

    static SellableItem.State toDomain(StateJpa s) {
        return s == null ? SellableItem.State.AVAILABLE : SellableItem.State.valueOf(s.name());
    }

    static StateJpa toJpa(SellableItem.State s) {
        return s == null ? StateJpa.AVAILABLE
                : StateJpa.valueOf(s.name());
    }
}
