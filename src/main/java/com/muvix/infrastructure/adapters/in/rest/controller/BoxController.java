package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.event.CreateRelocationEventUseCase;
import com.muvix.application.port.in.event.ManageBoxUseCase;
import com.muvix.application.port.in.event.ManageRelocationEventUseCase;
import com.muvix.domain.model.management.Box;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;

@RestController
@RequestMapping("/api/v1/boxes")
public class


BoxController {

    private final CreateRelocationEventUseCase createBoxUseCase;
    private final ManageBoxUseCase manageBoxUseCase;
    private final ManageRelocationEventUseCase manageEvents;

    public BoxController(CreateRelocationEventUseCase createBoxUseCase,
                         ManageBoxUseCase manageBoxUseCase, ManageRelocationEventUseCase manageEvents) {
        this.createBoxUseCase = createBoxUseCase;
        this.manageBoxUseCase = manageBoxUseCase;
        this.manageEvents = manageEvents;
    }

    @PostMapping("/sale")
    public ResponseEntity<Box> createSaleBox(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateRelocationEventUseCase.CreateSaleBoxCommand command) {
        Box box = createBoxUseCase.createSaleBox(new CreateRelocationEventUseCase.CreateSaleBoxCommand(userId(jwt),command.title(),command.originLocation(),command.saleDeadline(),command.takeItAllPrice(),command.discountRules()));
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/purchase")
    public ResponseEntity<Box> createPurchaseBox(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateRelocationEventUseCase.CreatePurchaseBoxCommand command) {
        Box box = createBoxUseCase.createPurchaseBox(new CreateRelocationEventUseCase.CreatePurchaseBoxCommand(userId(jwt),command.title(),command.destinationLocation(),command.arrivalDeliveryDate()));
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/full-relocation")
    public ResponseEntity<Box> createFullRelocationBox(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CreateRelocationEventUseCase.CreateFullRelocationBoxCommand command) {
        Box box = createBoxUseCase.createFullRelocationBox(new CreateRelocationEventUseCase.CreateFullRelocationBoxCommand(userId(jwt),command.title(),command.originLocation(),command.destinationLocation(),command.saleDeadline(),command.arrivalDeliveryDate(),command.takeItAllPrice(),command.discountRules()));
        return ResponseEntity.status(HttpStatus.CREATED).body(box);
    }

    @PostMapping("/{boxId}/items")
    public ResponseEntity<Void> addItemToBox(@PathVariable Long boxId,
                                             @AuthenticationPrincipal Jwt jwt,
                                             @RequestParam Long itemId) {
        manageBoxUseCase.addItemToBox(new ManageBoxUseCase.AddItemToBoxCommand(userId(jwt), boxId, itemId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boxId}")
    public ResponseEntity<Box> get(@AuthenticationPrincipal Jwt jwt,@PathVariable Long boxId){return ResponseEntity.ok(manageEvents.get(boxId,userId(jwt)));}

    @PutMapping("/{boxId}")
    public ResponseEntity<Box> replace(@AuthenticationPrincipal Jwt jwt,@PathVariable Long boxId,@RequestBody ReplaceBoxRequest r){
        return ResponseEntity.ok(manageEvents.replace(new ManageRelocationEventUseCase.ReplaceCommand(boxId,userId(jwt),r.title(),r.originLocation(),r.destinationLocation(),r.saleDeadline(),r.arrivalDeliveryDate(),r.takeItAllPrice(),r.discountRules())));
    }

    @DeleteMapping("/{boxId}")
    public ResponseEntity<Void> cancel(@AuthenticationPrincipal Jwt jwt,@PathVariable Long boxId){manageEvents.cancel(boxId,userId(jwt));return ResponseEntity.noContent().build();}

    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record ReplaceBoxRequest(String title,com.muvix.domain.model.logistic.Location originLocation,
            com.muvix.domain.model.logistic.Location destinationLocation,java.time.Instant saleDeadline,
            java.time.Instant arrivalDeliveryDate,java.math.BigDecimal takeItAllPrice,
            java.util.List<com.muvix.domain.model.management.DiscountRule> discountRules){}
}
