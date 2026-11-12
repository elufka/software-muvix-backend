
package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.EscrowAccountJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.EscrowPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.EscrowAccountSpringDataJpaRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class EscrowAccountMySQLAdapter implements EscrowAccountRepositoryPort {

    private final EscrowAccountSpringDataJpaRepository jpaRepository;

    public EscrowAccountMySQLAdapter(
            EscrowAccountSpringDataJpaRepository jpaRepository
    ) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EscrowAccount save(EscrowAccount escrowAccount) {

        EscrowAccountJpaEntity entity =
                EscrowPersistenceMapper.toEntity(escrowAccount);

        EscrowAccountJpaEntity saved = jpaRepository.save(entity);

        return EscrowPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<EscrowAccount> findByOrderId(Long orderId) {

        return jpaRepository.findById(orderId)
                .map(EscrowPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscrowAccount> findPendingReleaseAccounts(
            Instant deliveredBefore
    ) {

        return jpaRepository
                .findByStatusAndFundedAtBefore(
                        EscrowAccountJpaEntity.EscrowStatusJpa.HELD,
                        deliveredBefore
                )
                .stream()
                .map(EscrowPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EscrowAccount> findByStatus(
            EscrowAccount.EscrowStatus status
    ) {

        EscrowAccountJpaEntity.EscrowStatusJpa jpaStatus =
                EscrowAccountJpaEntity.EscrowStatusJpa.valueOf(
                        status.name()
                );

        return jpaRepository.findByStatus(jpaStatus)
                .stream()
                .map(EscrowPersistenceMapper::toDomain)
                .toList();
    }
}
