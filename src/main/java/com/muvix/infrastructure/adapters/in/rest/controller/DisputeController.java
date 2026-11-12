package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.OpenDisputeUseCase;
import com.muvix.domain.model.transaction.Dispute;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disputes")
public class DisputeController {

    private final OpenDisputeUseCase openDisputeUseCase;

    public DisputeController(OpenDisputeUseCase openDisputeUseCase) {
        this.openDisputeUseCase = openDisputeUseCase;
    }

    @PostMapping
    public ResponseEntity<Dispute> openDispute(@Valid @RequestBody OpenDisputeUseCase.OpenDisputeCommand command) {
        Dispute dispute = openDisputeUseCase.openDispute(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(dispute);
    }
}