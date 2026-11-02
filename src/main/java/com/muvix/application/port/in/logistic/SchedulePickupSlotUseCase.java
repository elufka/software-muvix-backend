package com.muvix.application.port.in.logistic;

import com.muvix.domain.model.logistic.PickupSlot;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;

import java.time.Instant;

public interface SchedulePickupSlotUseCase {

    record ProposeSlotCommand(
            Long orderId,
            Long buyerId,
            Instant startTime,
            Instant endTime,
            AuthorizedPickupPerson authorizedPerson
    ) {}

    record CounterProposeSlotCommand(
            Long orderId,
            Long sellerId,
            Long slotId,
            Instant newStartTime,
            Instant newEndTime
    ) {}

    PickupSlot proposeSlot(ProposeSlotCommand command);
    PickupSlot counterProposeSlot(CounterProposeSlotCommand command);
    void confirmSlot(Long orderId, Long slotId, Long userId);
}