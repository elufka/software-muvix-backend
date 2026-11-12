package com.muvix.application.port.in.transaction;

public interface ReleaseEscrowUseCase {
    void releaseFunds(Long orderId, String escrowId);
}