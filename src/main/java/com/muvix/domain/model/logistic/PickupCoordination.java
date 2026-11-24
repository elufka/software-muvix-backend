package com.muvix.domain.model.logistic;

import java.time.Instant;

public record PickupCoordination(Long id, Long orderId, Long proposerId, Instant startTime,
                                 Instant endTime, AuthorizedPickupPerson authorizedPerson,
                                 Status status, boolean buyerAgreedInChat, boolean sellerAgreedInChat,
                                 Instant createdAt) {
    public enum Status { PROPOSED, AWAITING_CONFIRMATION, CONFIRMED, DECLINED, SUPERSEDED }
}