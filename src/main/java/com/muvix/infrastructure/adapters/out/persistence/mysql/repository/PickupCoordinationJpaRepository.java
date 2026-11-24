package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.PickupCoordinationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface PickupCoordinationJpaRepository extends JpaRepository<PickupCoordinationJpaEntity,Long> {
    Optional<PickupCoordinationJpaEntity> findFirstByOrderIdOrderByIdDesc(Long orderId);

    @Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PickupCoordinationJpaEntity p where p.id=:id")
    Optional<PickupCoordinationJpaEntity> lockById(@Param("id") Long id);
}