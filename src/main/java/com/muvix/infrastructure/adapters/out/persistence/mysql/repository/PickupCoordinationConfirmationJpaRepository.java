package com.muvix.infrastructure.adapters.out.persistence.mysql.repository;

import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.PickupCoordinationConfirmationJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PickupCoordinationConfirmationJpaRepository extends JpaRepository<PickupCoordinationConfirmationJpaEntity,Long> {
    Optional<PickupCoordinationConfirmationJpaEntity> findByProposalIdAndUserId(Long proposalId,Long userId);
}