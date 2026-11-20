package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.StateJpa;

import java.util.List;

public interface SellableItemJpaRepository extends JpaRepository<SellableItemJpaEntity,Long>, JpaSpecificationExecutor<SellableItemJpaEntity> {
    List<SellableItemJpaEntity> findBySellerId(Long sellerId);

    boolean existsBySellerIdAndState(Long sellerId, StateJpa state);
}
