package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.ManageOfferUseCase;
import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.domain.model.negotiation.Offer;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/offers")
public class OfferController {
    private final ManageOfferUseCase manageOffers;
    private final PlaceOrderUseCase placeOrder;
    public OfferController(ManageOfferUseCase manageOffers, PlaceOrderUseCase placeOrder) {
        this.manageOffers = manageOffers; this.placeOrder = placeOrder;
    }
    @PostMapping
    public ResponseEntity<Offer> create(@AuthenticationPrincipal Jwt jwt, @RequestBody CreateOfferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(manageOffers.create(
                new ManageOfferUseCase.CreateOfferCommand(Long.valueOf(jwt.getSubject()), request.itemId(), request.amount())));
    }
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<OrderController.CheckoutResponse> accept(@AuthenticationPrincipal Jwt jwt,
                                                                   @PathVariable Long offerId,
                                                                   @RequestBody(required = false) PickupProposalRequest request) {
        PlaceOrderUseCase.PickupSlotProposal pickupSlot = request == null ? null
                : OrderController.pickupProposal(request.pickupStartTime(), request.pickupEndTime(), request.authorizedPerson());
        PlaceOrderUseCase.CheckoutResult result = placeOrder.executeOfferPurchase(
                new PlaceOrderUseCase.AcceptOfferPurchaseCommand(Long.valueOf(jwt.getSubject()), offerId, pickupSlot));
        return ResponseEntity.status(HttpStatus.CREATED).body(OrderController.toCheckoutResponse(result));
    }
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Offer> reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long offerId) {
        return ResponseEntity.ok(manageOffers.reject(offerId, Long.valueOf(jwt.getSubject())));
    }
    @GetMapping("/{offerId}")
    public ResponseEntity<Offer> get(@AuthenticationPrincipal Jwt jwt,@PathVariable Long offerId){
        return ResponseEntity.ok(manageOffers.get(offerId,Long.valueOf(jwt.getSubject())));
    }
    public record CreateOfferRequest(Long itemId, BigDecimal amount) {}
    public record PickupProposalRequest(java.time.Instant pickupStartTime, java.time.Instant pickupEndTime,
                                        com.muvix.domain.model.logistic.AuthorizedPickupPerson authorizedPerson) {}
}
