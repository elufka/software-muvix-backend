package com.muvix.infrastructure.adapters.out.persistence.mysql.adapter;

import com.muvix.application.port.out.persistence.OrderRepositoryPort;
import com.muvix.domain.model.transaction.Order;
import com.muvix.infrastructure.adapters.out.persistence.mysql.mapper.OrderPersistenceMapper;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderJpaEntity;
import com.muvix.infrastructure.adapters.out.persistence.mysql.repository.OrderSpringDataJpaRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.List;
import java.time.Instant;

@Component
@Transactional
public class OrderMysqlAdapter implements OrderRepositoryPort {

    private final OrderSpringDataJpaRepository repository;

    public OrderMysqlAdapter(OrderSpringDataJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public Order save(Order order) {
        return OrderPersistenceMapper.toDomain(repository.save(OrderPersistenceMapper.toEntity(order)));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Order> findById(Long id) {
        return repository.findById(id).map(OrderPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findDeliveredBefore(Instant deliveredBefore) {
        return repository.findByStatusAndDeliveredAtBefore(OrderJpaEntity.StatusJpa.DELIVERED, deliveredBefore)
                .stream().map(OrderPersistenceMapper::toDomain).toList();
    }
}
