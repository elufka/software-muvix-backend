package com.muvix.application.port.in.transaction;
import java.time.Instant;
public interface CancelOrderUseCase {
    void cancelByBuyer(Long orderId, Long buyerId);
    boolean cancelExpiredPendingSlot(Long orderId,Instant createdBefore);
}