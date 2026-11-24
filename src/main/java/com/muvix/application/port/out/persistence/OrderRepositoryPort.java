package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.transaction.Order;
import java.util.Optional;
import java.time.Instant;
import java.util.List;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
    List<Order> findDeliveredBefore(Instant deliveredBefore);
    List<Order> findPendingSlotBefore(Instant createdBefore);
}