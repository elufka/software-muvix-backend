package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.DisputeRepositoryPort;
import com.muvix.domain.model.transaction.Dispute;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.DisputePersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.DisputeSpringDataJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class DisputeMySQLAdapter implements DisputeRepositoryPort {

    private final DisputeSpringDataJpaRepository repository;

    public DisputeMySQLAdapter(DisputeSpringDataJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Dispute save(Dispute dispute) {
        return DisputePersistenceMapper.toDomain(repository.save(DisputePersistenceMapper.toEntity(dispute)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> findById(Long id) {
        return repository.findById(id).map(DisputePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Dispute> findByOrderId(Long orderId) {
        return repository.findByOrderId(orderId).map(DisputePersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findByBuyerId(Long buyerId) {
        return repository.findByBuyerId(buyerId).stream()
                .map(DisputePersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Dispute> findByStatus(Dispute.Status status) {
        return repository.findByStatus(com.muvix.infrastructure.adapters.out.persistence.mysql.entity.DisputeJpaEntity.StatusJpa.valueOf(status.name()))
                .stream().map(DisputePersistenceMapper::toDomain).toList();
    }
}
