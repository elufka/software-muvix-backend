package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.logistic.SchedulePickupSlotUseCase;
import com.muvix.domain.model.logistic.PickupSlot;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pickups")
public class PickupController {

    private final SchedulePickupSlotUseCase schedulePickupSlotUseCase;

    public PickupController(SchedulePickupSlotUseCase schedulePickupSlotUseCase) {
        this.schedulePickupSlotUseCase = schedulePickupSlotUseCase;
    }

    @PostMapping("/slots/propose")
    public ResponseEntity<PickupSlot> proposeSlot(@Valid @RequestBody SchedulePickupSlotUseCase.ProposeSlotCommand command) {
        PickupSlot slot = schedulePickupSlotUseCase.proposeSlot(command);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/slots/counter-propose")
    public ResponseEntity<PickupSlot> counterProposeSlot(@Valid @RequestBody SchedulePickupSlotUseCase.CounterProposeSlotCommand command) {
        PickupSlot slot = schedulePickupSlotUseCase.counterProposeSlot(command);
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/orders/{orderId}/slots/{slotId}/confirm")
    public ResponseEntity<Void> confirmSlot(@PathVariable Long orderId,
                                            @PathVariable Long slotId,
                                            @RequestParam Long userId) {
        schedulePickupSlotUseCase.confirmSlot(orderId, slotId, userId);
        return ResponseEntity.ok().build();
    }
}
