package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.OpenDisputeUseCase;
import com.muvix.application.port.in.transaction.ManageDisputeUseCase;
import com.muvix.domain.model.transaction.Dispute;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeController {

    private final OpenDisputeUseCase openDisputeUseCase;
    private final ManageDisputeUseCase manageDisputeUseCase;

    public DisputeController(OpenDisputeUseCase openDisputeUseCase, ManageDisputeUseCase manageDisputeUseCase) {
        this.openDisputeUseCase = openDisputeUseCase;
        this.manageDisputeUseCase = manageDisputeUseCase;
    }

    @PostMapping
    public ResponseEntity<Dispute> openDispute(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody OpenDisputeRequest request) {
        Dispute dispute = openDisputeUseCase.openDispute(new OpenDisputeUseCase.OpenDisputeCommand(request.orderId(),Long.valueOf(jwt.getSubject()),request.reason(),request.evidenceImages()));
        return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
    }

    @PostMapping("/{disputeId}/evidence")
    public ResponseEntity<Dispute> submitEvidence(@AuthenticationPrincipal Jwt jwt,@PathVariable Long disputeId,
                                                   @RequestBody java.util.List<String> evidenceImages) {
        return ResponseEntity.ok(manageDisputeUseCase.submitAdditionalEvidence(
                new ManageDisputeUseCase.SubmitEvidenceCommand(disputeId, Long.valueOf(jwt.getSubject()), evidenceImages)));
    }

    @GetMapping("/{disputeId}")
    public ResponseEntity<Dispute> get(@AuthenticationPrincipal Jwt jwt,@PathVariable Long disputeId){
        java.util.List<String> roles=jwt.getClaimAsStringList("roles");
        boolean moderator=roles!=null&&roles.contains("MODERATOR");
        return ResponseEntity.ok(manageDisputeUseCase.getVisible(disputeId,Long.valueOf(jwt.getSubject()),moderator));
    }

    @PostMapping("/{disputeId}/moderator-decision")
    @PreAuthorize("hasRole('MODERATOR')")
    public ResponseEntity<Dispute> moderatorDecision(@PathVariable Long disputeId,
            @RequestBody ModeratorDecisionRequest request) {
        return ResponseEntity.ok(manageDisputeUseCase.applyModeratorDecision(disputeId, request.decision()));
    }

    public record ModeratorDecisionRequest(ManageDisputeUseCase.ModeratorDecision decision) {}
    public record OpenDisputeRequest(Long orderId,String reason,java.util.List<String> evidenceImages){}
}
