package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.BatchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BatchJpaRepository extends JpaRepository<BatchJpaEntity, Long> {}