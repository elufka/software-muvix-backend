package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.DisputeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DisputeSpringDataJpaRepository extends JpaRepository<DisputeJpaEntity, Long> {

    Optional<DisputeJpaEntity> findByOrderId(Long orderId);

    List<DisputeJpaEntity> findByBuyerId(Long buyerId);

    List<DisputeJpaEntity> findByStatus(DisputeJpaEntity.StatusJpa status);
}
