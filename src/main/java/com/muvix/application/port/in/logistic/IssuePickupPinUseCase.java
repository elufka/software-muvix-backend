package com.muvix.application.port.in.logistic;

public interface IssuePickupPinUseCase {
    /** Issues or rotates the pickup PIN and returns its raw value once to the requesting buyer. */
    String issue(Long orderId, Long buyerId);
}