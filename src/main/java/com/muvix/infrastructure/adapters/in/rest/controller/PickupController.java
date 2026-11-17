package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.logistic.SchedulePickupSlotUseCase;
import com.muvix.domain.model.logistic.PickupSlot;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/pickups")
public class PickupController {

    private final SchedulePickupSlotUseCase schedulePickupSlotUseCase;

    public PickupController(SchedulePickupSlotUseCase schedulePickupSlotUseCase) {
        this.schedulePickupSlotUseCase = schedulePickupSlotUseCase;
    }

    @PostMapping("/slots/propose")
    public ResponseEntity<PickupSlot> proposeSlot(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ProposeRequest request) {
        PickupSlot slot = schedulePickupSlotUseCase.proposeSlot(new SchedulePickupSlotUseCase.ProposeSlotCommand(
                request.orderId(),userId(jwt),request.startTime(),request.endTime(),request.authorizedPerson()));
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/slots/counter-propose")
    public ResponseEntity<PickupSlot> counterProposeSlot(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CounterRequest request) {
        PickupSlot slot = schedulePickupSlotUseCase.counterProposeSlot(new SchedulePickupSlotUseCase.CounterProposeSlotCommand(
                request.orderId(),userId(jwt),request.slotId(),request.newStartTime(),request.newEndTime()));
        return ResponseEntity.ok(slot);
    }

    @PostMapping("/orders/{orderId}/slots/{slotId}/confirm")
    public ResponseEntity<Void> confirmSlot(@PathVariable Long orderId,
                                            @PathVariable Long slotId,
                                            @AuthenticationPrincipal Jwt jwt) {
        schedulePickupSlotUseCase.confirmSlot(orderId, slotId, userId(jwt));
        return ResponseEntity.ok().build();
    }
    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record ProposeRequest(Long orderId,java.time.Instant startTime,java.time.Instant endTime,
                                 com.muvix.domain.model.logistic.AuthorizedPickupPerson authorizedPerson){}
    public record CounterRequest(Long orderId,Long slotId,java.time.Instant newStartTime,java.time.Instant newEndTime){}
}
