package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.PickupSlotJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PickupSlotJpaRepository extends JpaRepository<PickupSlotJpaEntity, Long> {
    java.util.Optional<PickupSlotJpaEntity> findFirstByOrderIdOrderByIdDesc(Long orderId);
}
