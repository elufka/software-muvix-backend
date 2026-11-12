package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.SellableItemJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SellableItemJpaRepository extends JpaRepository<SellableItemJpaEntity,Long> {
    List<SellableItemJpaEntity> findBySellerId(Long sellerId);
}
