package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.CartAndCheckoutUseCase;
import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import java.util.Set;
import java.time.Instant;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
import com.muvix.domain.model.transaction.Order;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final PlaceOrderUseCase placeOrderUseCase;
    private final CartAndCheckoutUseCase cartAndCheckoutUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase,
                           CartAndCheckoutUseCase cartAndCheckoutUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.cartAndCheckoutUseCase = cartAndCheckoutUseCase;
    }

    @PostMapping("/direct")
    public ResponseEntity<CartAndCheckoutUseCase.CheckoutResult> placeDirectOrder(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody DirectOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeOrderUseCase.executeDirectPurchase(new PlaceOrderUseCase.DirectPurchaseCommand(userId(jwt),request.eventId(),request.itemIds())));
    }

    @PostMapping("/take-it-all")
    public ResponseEntity<CartAndCheckoutUseCase.CheckoutResult> placeTakeItAllOrder(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody TakeItAllRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeOrderUseCase.executeTakeItAllPurchase(new PlaceOrderUseCase.TakeItAllPurchaseCommand(userId(jwt),request.eventId())));
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<CartAndCheckoutUseCase.CheckoutResult> checkoutCart(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody CartCheckoutRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartAndCheckoutUseCase.checkoutWithPayment(new CartAndCheckoutUseCase.CheckoutCartCommand(userId(jwt),request.targetEventId(),request.deliveryMethod())));
    }

    @PostMapping("/cart/items")
    public ResponseEntity<Void> addItemToCart(@AuthenticationPrincipal Jwt jwt,@RequestParam Long itemId) {
        cartAndCheckoutUseCase.addItemToCart(new CartAndCheckoutUseCase.AddToCartCommand(userId(jwt), itemId));
        return ResponseEntity.ok().build();
    }

    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record DirectOrderRequest(Long eventId,Set<Long> itemIds){}
    public record TakeItAllRequest(Long eventId){}
    public record CartCheckoutRequest(Long targetEventId,Order.DeliveryMethod deliveryMethod){}
}
