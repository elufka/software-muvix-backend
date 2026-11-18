package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.logistic.PickupSlot;
import java.util.Optional;

public interface PickupSlotRepositoryPort {
    PickupSlot save(PickupSlot slot);
    Optional<PickupSlot> findById(Long id);
    Optional<PickupSlot> findLatestByOrderId(Long orderId);
}
