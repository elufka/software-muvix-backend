package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Dispute;

import java.util.List;

public interface OpenDisputeUseCase {

    record OpenDisputeCommand(
            Long orderId,
            Long buyerId,
            String reason,
            List<String> evidenceImages
    ) {}

    Dispute openDispute(OpenDisputeCommand command);
}