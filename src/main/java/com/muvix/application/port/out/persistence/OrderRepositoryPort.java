package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.transaction.Order;
import java.util.Optional;

public interface OrderRepositoryPort {
    Order save(Order order);
    Optional<Order> findById(Long id);
}