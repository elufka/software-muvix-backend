package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.logistic.SchedulePickupSlotUseCase;
import com.muvix.application.port.in.logistic.CompletePickupUseCase;
import com.muvix.application.port.in.logistic.IssuePickupPinUseCase;
import com.muvix.domain.model.logistic.PickupSlot;
import jakarta.validation.Valid;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/pickups")
public class PickupController {

    private final SchedulePickupSlotUseCase schedulePickupSlotUseCase;
    private final CompletePickupUseCase completePickupUseCase;
    private final IssuePickupPinUseCase issuePickupPinUseCase;

    public PickupController(SchedulePickupSlotUseCase schedulePickupSlotUseCase,
                            CompletePickupUseCase completePickupUseCase,
                            IssuePickupPinUseCase issuePickupPinUseCase) {
        this.schedulePickupSlotUseCase = schedulePickupSlotUseCase;
        this.completePickupUseCase = completePickupUseCase;
        this.issuePickupPinUseCase = issuePickupPinUseCase;
    }

    @PostMapping("/slots/propose")
    public ResponseEntity<PickupSlot> proposeSlot(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody ProposeRequest request) {
        PickupSlot slot = schedulePickupSlotUseCase.proposeSlot(new SchedulePickupSlotUseCase.ProposeSlotCommand(
                request.orderId(),userId(jwt),request.startTime(),request.endTime(),request.authorizedPerson()));
        return ResponseEntity.ok(slot);
    }

    @GetMapping("/slots/{slotId}")
    public ResponseEntity<PickupSlot> getSlot(@AuthenticationPrincipal Jwt jwt,@PathVariable Long slotId){
        return ResponseEntity.ok(schedulePickupSlotUseCase.getSlot(slotId,userId(jwt)));
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

    @PostMapping("/orders/{orderId}/complete")
    public ResponseEntity<Void> completePickup(@PathVariable Long orderId,
                                               @AuthenticationPrincipal Jwt jwt,
                                               @Valid @RequestBody CompletePickupRequest request) {
        completePickupUseCase.complete(orderId, userId(jwt), request.pickupPin());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/orders/{orderId}/pin")
    public ResponseEntity<PickupPinResponse> requestPickupPin(@PathVariable Long orderId,
                                                              @AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore())
                .body(new PickupPinResponse(issuePickupPinUseCase.issue(orderId, userId(jwt))));
    }

    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record ProposeRequest(Long orderId,java.time.Instant startTime,java.time.Instant endTime,
                                 com.muvix.domain.model.logistic.AuthorizedPickupPerson authorizedPerson){}
    public record CounterRequest(Long orderId,Long slotId,java.time.Instant newStartTime,java.time.Instant newEndTime){}
    public record CompletePickupRequest(String pickupPin) {}
    public record PickupPinResponse(String pickupPin) {}
}
