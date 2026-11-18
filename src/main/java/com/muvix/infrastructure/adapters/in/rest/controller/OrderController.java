package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.application.port.in.transaction.GetOrderUseCase;
import com.muvix.application.port.in.transaction.CancelOrderUseCase;
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
    private final GetOrderUseCase getOrderUseCase;
    private final CancelOrderUseCase cancelOrderUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, GetOrderUseCase getOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
    }

    @PostMapping("/direct")
    public ResponseEntity<PlaceOrderUseCase.CheckoutResult> placeDirectOrder(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody DirectOrderRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeOrderUseCase.executeDirectPurchase(new PlaceOrderUseCase.DirectPurchaseCommand(userId(jwt),request.eventId(),request.itemIds())));
    }

    @PostMapping("/take-it-all")
    public ResponseEntity<PlaceOrderUseCase.CheckoutResult> placeTakeItAllOrder(@AuthenticationPrincipal Jwt jwt,@Valid @RequestBody TakeItAllRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(placeOrderUseCase.executeTakeItAllPurchase(new PlaceOrderUseCase.TakeItAllPurchaseCommand(userId(jwt),request.eventId())));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrder(@AuthenticationPrincipal Jwt jwt,@PathVariable Long orderId){
        return ResponseEntity.ok(getOrderUseCase.get(orderId,userId(jwt)));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@AuthenticationPrincipal Jwt jwt,@PathVariable Long orderId){
        cancelOrderUseCase.cancelByBuyer(orderId,userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Long userId(Jwt jwt){return Long.valueOf(jwt.getSubject());}
    public record DirectOrderRequest(Long eventId,Set<Long> itemIds){}
    public record TakeItAllRequest(Long eventId){}
}
