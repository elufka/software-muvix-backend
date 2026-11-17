package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.RelocationEventRepositoryPort;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RelocationEventJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.RelocationEventPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.RelocationEventJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Component
@Transactional
public class RelocationEventMySQLAdapter implements RelocationEventRepositoryPort {

    private final RelocationEventJpaRepository repository;

    public RelocationEventMySQLAdapter(RelocationEventJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public RelocationEvent save(RelocationEvent event) {
        RelocationEventJpaEntity existing = event.getId() == null
                ? null
                : repository.findById(event.getId())
                        .orElseThrow(() -> new DomainException("Relocation event not found: " + event.getId()));
        RelocationEventJpaEntity saved = repository.save(
                RelocationEventPersistenceMapper.toEntity(event, existing));
        return RelocationEventPersistenceMapper.toDomain(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelocationEvent> findById(Long id) {
        return repository.findById(id).map(RelocationEventPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RelocationEvent> findByShareToken(String shareToken) {
        return repository.findByShareToken(shareToken).map(RelocationEventPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RelocationEvent> findByUserId(Long userId) {
        return repository.findByUserId(userId).stream()
                .map(RelocationEventPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> findItemIdsByEventId(Long eventId) {
        return repository.findItemIdsByEventId(eventId);
    }

    @Override
    public void linkItemToBox(Long boxId, Long itemId) {
        RelocationEventJpaEntity event = repository.findById(boxId)
                .orElseThrow(() -> new DomainException("Relocation event not found: " + boxId));
        event.getItemIds().add(itemId);
    }

    @Override
    public void unlinkItemFromBox(Long boxId, Long itemId) {
        RelocationEventJpaEntity event = repository.findById(boxId)
                .orElseThrow(() -> new DomainException("Relocation event not found: " + boxId));
        event.getItemIds().remove(itemId);
    }
}
