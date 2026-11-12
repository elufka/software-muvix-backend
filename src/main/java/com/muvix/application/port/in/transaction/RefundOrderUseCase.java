package com.muvix.application.port.in.transaction;

public interface RefundOrderUseCase {
    void refundOrder(Long orderId, String reason);
}