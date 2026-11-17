package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderSpringDataJpaRepository extends JpaRepository<OrderJpaEntity, Long> {

    List<OrderJpaEntity> findByBuyerId(Long buyerId);

    List<OrderJpaEntity> findByEventIdIn(List<Long> eventIds);

    List<OrderJpaEntity> findByStatusAndDeliveredAtBefore(OrderJpaEntity.StatusJpa status, java.time.Instant deliveredBefore);
}
