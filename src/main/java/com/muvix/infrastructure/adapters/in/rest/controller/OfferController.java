package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.ManageOfferUseCase;
import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.application.port.in.transaction.CartAndCheckoutUseCase;
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
    public ResponseEntity<CartAndCheckoutUseCase.CheckoutResult> accept(@AuthenticationPrincipal Jwt jwt,
                                                                         @PathVariable Long offerId) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeOrder.executeOfferPurchase(
                new PlaceOrderUseCase.AcceptOfferPurchaseCommand(Long.valueOf(jwt.getSubject()), offerId)));
    }
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Offer> reject(@AuthenticationPrincipal Jwt jwt, @PathVariable Long offerId) {
        return ResponseEntity.ok(manageOffers.reject(offerId, Long.valueOf(jwt.getSubject())));
    }
    public record CreateOfferRequest(Long itemId, BigDecimal amount) {}
}
