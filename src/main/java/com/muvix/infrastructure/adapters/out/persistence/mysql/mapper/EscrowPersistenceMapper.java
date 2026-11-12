
package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.EscrowAccountJpaEntity;

public final class EscrowPersistenceMapper {

    private EscrowPersistenceMapper() {
    }

    public static EscrowAccount toDomain(EscrowAccountJpaEntity entity) {

        if (entity == null) {
            return null;
        }

        return EscrowAccount.restore(
                entity.getOrderId(),
                entity.getAmount(),
                EscrowAccount.EscrowStatus.valueOf(
                        entity.getStatus().name()
                ),
                entity.getExternalOperationId(),
                entity.getFundedAt(),
                entity.getAccreditedAt()
        );
    }

    public static EscrowAccountJpaEntity toEntity(EscrowAccount domain) {

        if (domain == null) {
            return null;
        }

        EscrowAccountJpaEntity entity = new EscrowAccountJpaEntity();

        entity.setOrderId(domain.getOrderId());
        entity.setAmount(domain.getAmount());

        entity.setStatus(
                EscrowAccountJpaEntity.EscrowStatusJpa.valueOf(
                        domain.getStatus().name()
                )
        );

        entity.setExternalOperationId(domain.getExternalOperationId());
        entity.setFundedAt(domain.getFundedAt());
        entity.setAccreditedAt(domain.getAccreditedAt());

        return entity;
    }
}
