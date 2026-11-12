package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.event.CreateRelocationEventUseCase;
import com.muvix.application.port.in.event.ManageBoxUseCase;
import com.muvix.domain.model.management.RelocationEvent;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boxes")
public class BoxController {

    private final CreateRelocationEventUseCase createBoxUseCase;
    private final ManageBoxUseCase manageBoxUseCase;

    public BoxController(CreateRelocationEventUseCase createBoxUseCase,
                         ManageBoxUseCase manageBoxUseCase) {
        this.createBoxUseCase = createBoxUseCase;
        this.manageBoxUseCase = manageBoxUseCase;
    }

    @PostMapping("/sale")
    public ResponseEntity<RelocationEvent> createSaleBox(@Valid @RequestBody CreateRelocationEventUseCase.CreateSaleBoxCommand command) {
        RelocationEvent box = createBoxUseCase.createSaleBox(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/purchase")
    public ResponseEntity<RelocationEvent> createPurchaseBox(@Valid @RequestBody CreateRelocationEventUseCase.CreatePurchaseBoxCommand command) {
        RelocationEvent box = createBoxUseCase.createPurchaseBox(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/full-relocation")
    public ResponseEntity<RelocationEvent> createFullRelocationBox(@Valid @RequestBody CreateRelocationEventUseCase.CreateFullRelocationBoxCommand command) {
        RelocationEvent box = createBoxUseCase.createFullRelocationBox(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/{boxId}/items")
    public ResponseEntity<Void> addItemToBox(@PathVariable Long boxId,
                                             @RequestParam Long userId,
                                             @RequestParam Long itemId) {
        manageBoxUseCase.addItemToBox(new ManageBoxUseCase.AddItemToBoxCommand(userId, boxId, itemId));
        return ResponseEntity.ok().build();
    }
}