package com.muvix.application.port.in.logistic;

public interface CompletePickupUseCase {
    void complete(Long orderId, Long sellerId, String pickupPin);
}
