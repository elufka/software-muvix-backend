package com.muvix.application.port.in.transaction;
public interface CancelOrderUseCase { void cancelByBuyer(Long orderId, Long buyerId); }
