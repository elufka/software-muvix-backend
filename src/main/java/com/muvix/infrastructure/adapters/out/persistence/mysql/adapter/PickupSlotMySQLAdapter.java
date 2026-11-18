package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.PickupSlotRepositoryPort;
import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.PickupSlotJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.PickupSlotJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Component
@Transactional
public class PickupSlotMySQLAdapter implements PickupSlotRepositoryPort {
    private final PickupSlotJpaRepository repository;
    public PickupSlotMySQLAdapter(PickupSlotJpaRepository repository) { this.repository = repository; }
    @Override public PickupSlot save(PickupSlot slot) {
        PickupSlotJpaEntity entity = slot.getId() == null ? new PickupSlotJpaEntity()
                : repository.findById(slot.getId()).orElseGet(PickupSlotJpaEntity::new);
        entity.setId(slot.getId()); entity.setOrderId(slot.getOrderId());
        entity.setStartTime(slot.getProposedStartTime()); entity.setEndTime(slot.getProposedEndTime());
        entity.setStatus(slot.getStatus()); entity.setBuyerConfirmed(slot.isBuyerConfirmed());
        entity.setSellerConfirmed(slot.isSellerConfirmed());
        return toDomain(repository.save(entity));
    }
    @Override @Transactional(readOnly = true) public Optional<PickupSlot> findById(Long id) {
        return repository.findById(id).map(PickupSlotMySQLAdapter::toDomain);
    }
    @Override @Transactional(readOnly = true) public Optional<PickupSlot> findLatestByOrderId(Long orderId) {
        return repository.findFirstByOrderIdOrderByIdDesc(orderId).map(PickupSlotMySQLAdapter::toDomain);
    }
    private static PickupSlot toDomain(PickupSlotJpaEntity e) {
        return PickupSlot.restore(e.getId(), e.getOrderId(), e.getStartTime(), e.getEndTime(), e.getStatus(),
                e.isBuyerConfirmed(), e.isSellerConfirmed());
    }
}
