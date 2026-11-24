package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.event.*;
import com.muvix.domain.model.management.Box;
import com.muvix.domain.model.management.FullRelocationBox;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.mapper.BoxRestMapper;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.BoxResponse;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.InviteMemberRequest;
import com.muvix.infrastructure.adapters.in.rest.controller.Box.dtos.BoxDtos.ReplaceBoxRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/boxes")
@RequiredArgsConstructor
public class BoxController {

    private final CreateRelocationEventUseCase createBoxUseCase;
    private final ManageBoxUseCase manageBoxUseCase;
    private final ManageRelocationEventUseCase manageEvents;
    private final BoxQueryUseCase boxQueryUseCase;
    private final ManageBoxMembersUseCase manageBoxMembersUseCase;
    private final BoxRestMapper mapper;

    @PostMapping("/sale")
    public ResponseEntity<BoxResponse> createSaleBox(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRelocationEventUseCase.CreateSaleBoxCommand command) {

        Box box = createBoxUseCase.createSaleBox(
                new CreateRelocationEventUseCase.CreateSaleBoxCommand(
                        userId(jwt),
                        command.title(),
                        command.originLocation(),
                        command.saleDeadline(),
                        command.takeItAllPrice(),
                        command.discountRules()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(box));
    }

    @PostMapping("/purchase")
    public ResponseEntity<BoxResponse> createPurchaseBox(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRelocationEventUseCase.CreatePurchaseBoxCommand command) {

        Box box = createBoxUseCase.createPurchaseBox(
                new CreateRelocationEventUseCase.CreatePurchaseBoxCommand(
                        userId(jwt),
                        command.title(),
                        command.destinationLocation(),
                        command.arrivalDeliveryDate()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(box));
    }

    @PostMapping("/full-relocation")
    public ResponseEntity<BoxResponse> createFullRelocationBox(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody CreateRelocationEventUseCase.CreateFullRelocationBoxCommand command) {

        Box box = createBoxUseCase.createFullRelocationBox(
                new CreateRelocationEventUseCase.CreateFullRelocationBoxCommand(
                        userId(jwt),
                        command.title(),
                        command.originLocation(),
                        command.destinationLocation(),
                        command.saleDeadline(),
                        command.arrivalDeliveryDate(),
                        command.takeItAllPrice(),
                        command.discountRules()
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(box));
    }

    @PostMapping("/{boxId}/items")
    public ResponseEntity<Void> addItemToBox(
            @PathVariable Long boxId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam Long itemId) {

        manageBoxUseCase.addItemToBox(new ManageBoxUseCase.AddItemToBoxCommand(userId(jwt), boxId, itemId));
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{boxId}")
    public ResponseEntity<BoxResponse> get(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boxId) {

        Box box = manageEvents.get(boxId, userId(jwt));
        return ResponseEntity.ok(mapper.toResponse(box));
    }

    @GetMapping("/me/purchases")
    public ResponseEntity<List<BoxResponse>> getMyPurchaseBoxes(@AuthenticationPrincipal Jwt jwt) {
        List<BoxResponse> purchases = boxQueryUseCase.findPurchaseBoxesByUserId(userId(jwt)).stream()
                .map(mapper::toResponse)
                .toList();
        return ResponseEntity.ok(purchases);
    }

    @PostMapping("/{boxId}/members")
    public ResponseEntity<Void> inviteMember(
            @PathVariable Long boxId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody InviteMemberRequest request) {

        manageBoxMembersUseCase.inviteMember(mapper.toCommand(userId(jwt), boxId, request));
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PutMapping("/{boxId}")
    public ResponseEntity<BoxResponse> replace(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boxId,
            @Valid @RequestBody ReplaceBoxRequest request) {

        Box updated = manageEvents.replace(mapper.toCommand(userId(jwt), boxId, request));
        return ResponseEntity.ok(mapper.toResponse(updated));
    }

    @DeleteMapping("/{boxId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Long boxId) {

        manageEvents.cancel(boxId, userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Long userId(Jwt jwt) {
        return Long.valueOf(jwt.getSubject());
    }
}