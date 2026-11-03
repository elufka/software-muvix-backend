package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.EscrowAccountRepositoryPort;
import com.muvix.domain.model.transaction.EscrowAccount;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.EscrowAccountJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.EscrowAccountSpringDataJpaRepository;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class EscrowAccountMySQLAdapter implements EscrowAccountRepositoryPort {

    private final EscrowAccountSpringDataJpaRepository jpaRepository;

    public EscrowAccountMySQLAdapter(EscrowAccountSpringDataJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public EscrowAccount save(EscrowAccount escrowAccount) {
        EscrowAccountJpaEntity entity = toEntity(escrowAccount);
        EscrowAccountJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public Optional<EscrowAccount> findByOrderId(Long orderId) {
        return jpaRepository.findById(orderId).map(this::toDomain);
    }

    @Override
    public List<EscrowAccount> findPendingReleaseAccounts(Instant deliveredBefore) {
        return jpaRepository.findByStatus(EscrowAccountJpaEntity.EscrowStatusJpa.HELD).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<EscrowAccount> findByStatus(EscrowAccount.EscrowStatus status) {
        EscrowAccountJpaEntity.EscrowStatusJpa jpaStatus = EscrowAccountJpaEntity.EscrowStatusJpa.valueOf(status.name());
        return jpaRepository.findByStatus(jpaStatus).stream()
                .map(this::toDomain)
                .toList();
    }

    // Mappers de conversión entre Dominio y JPA
    private EscrowAccountJpaEntity toEntity(EscrowAccount domain) {
        EscrowAccountJpaEntity entity = new EscrowAccountJpaEntity();
        entity.setOrderId(domain.getOrderId());
        entity.setAmount(domain.getAmount());
        entity.setStatus(EscrowAccountJpaEntity.EscrowStatusJpa.valueOf(domain.getStatus().name()));
        entity.setFundedAt(domain.getFundedAt());
        entity.setAccreditedAt(domain.getAccreditedAt());
        return entity;
    }

    private EscrowAccount toDomain(EscrowAccountJpaEntity entity) {
        return new EscrowAccount(entity.getOrderId(), entity.getAmount());
    }
}