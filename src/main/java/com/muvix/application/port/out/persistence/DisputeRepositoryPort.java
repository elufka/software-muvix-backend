package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.transaction.Dispute;

import java.util.List;
import java.util.Optional;

public interface DisputeRepositoryPort {

    Dispute save(Dispute dispute);

    Optional<Dispute> findById(Long id);

    Optional<Dispute> findByOrderId(Long orderId);

    List<Dispute> findByBuyerId(Long buyerId);

    List<Dispute> findByStatus(Dispute.Status status);
}
