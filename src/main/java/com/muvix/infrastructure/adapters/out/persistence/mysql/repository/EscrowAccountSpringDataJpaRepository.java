
package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.EscrowAccountJpaEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface EscrowAccountSpringDataJpaRepository
        extends JpaRepository<EscrowAccountJpaEntity, Long> {

    List<EscrowAccountJpaEntity> findByStatus(
            EscrowAccountJpaEntity.EscrowStatusJpa status
    );

    List<EscrowAccountJpaEntity> findByStatusAndFundedAtBefore(
            EscrowAccountJpaEntity.EscrowStatusJpa status,
            Instant fundedBefore
    );
}
