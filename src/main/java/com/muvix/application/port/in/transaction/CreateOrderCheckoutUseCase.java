package com.muvix.application.port.in.transaction;
public interface CreateOrderCheckoutUseCase {
    String create(Long orderId, Long buyerId);
}