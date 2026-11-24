package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.OrderConfirmedEventJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderConfirmedEventJpaRepository extends JpaRepository<OrderConfirmedEventJpaEntity, Long> {

}