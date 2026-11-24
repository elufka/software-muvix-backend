package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.application.port.in.transaction.GetOrderUseCase;
import com.muvix.application.port.in.transaction.CancelOrderUseCase;
import com.muvix.application.port.in.transaction.CreateOrderCheckoutUseCase;
import com.muvix.infrastructure.adapters.in.rest.dto.response.OrderResponse;
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
    private final CreateOrderCheckoutUseCase createOrderCheckoutUseCase;

    public OrderController(PlaceOrderUseCase placeOrderUseCase, GetOrderUseCase getOrderUseCase,
                           CancelOrderUseCase cancelOrderUseCase,CreateOrderCheckoutUseCase createOrderCheckoutUseCase) {
        this.placeOrderUseCase = placeOrderUseCase;
        this.getOrderUseCase = getOrderUseCase;
        this.cancelOrderUseCase = cancelOrderUseCase;
        this.createOrderCheckoutUseCase=createOrderCheckoutUseCase;
    }

    @PostMapping("/direct") // mucha logica para un controller, cambiar
    public ResponseEntity<CheckoutResponse> placeDirectOrder(@AuthenticationPrincipal Jwt jwt,
                                                             @Valid @RequestBody DirectOrderRequest request) {
        PlaceOrderUseCase.CheckoutResult result = placeOrderUseCase.executeDirectPurchase(
                new PlaceOrderUseCase.DirectPurchaseCommand(
                        userId(jwt),
                        request.eventId(),
                        request.itemIds(),
                        pickupProposal(request.pickupStartTime(), request.pickupEndTime(), request.authorizedPerson())
                )
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(toCheckoutResponse(result));
    }

    @PostMapping("/take-it-all") // mucha logica para un controller, cambiar
    public ResponseEntity<CheckoutResponse> placeTakeItAllOrder(@AuthenticationPrincipal Jwt jwt,
                                                                @Valid @RequestBody TakeItAllRequest request) {
        PlaceOrderUseCase.CheckoutResult result = placeOrderUseCase.executeTakeItAllPurchase(
                new PlaceOrderUseCase.TakeItAllPurchaseCommand(userId(jwt), request.eventId(),
                        pickupProposal(request.pickupStartTime(), request.pickupEndTime(), request.authorizedPerson())));
        return ResponseEntity.status(HttpStatus.CREATED).body(toCheckoutResponse(result));
    }

    @PostMapping("/{orderId}/checkout")
    public ResponseEntity<CheckoutUrlResponse> createCheckout(@AuthenticationPrincipal Jwt jwt,
                                                              @PathVariable Long orderId){
        return ResponseEntity.ok(new CheckoutUrlResponse(createOrderCheckoutUseCase.create(orderId,userId(jwt))));
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@AuthenticationPrincipal Jwt jwt, @PathVariable Long orderId){
        Long actorId = userId(jwt);
        Order order = getOrderUseCase.get(orderId, actorId);
        return ResponseEntity.ok(OrderResponse.from(order));
    }

    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> cancelOrder(@AuthenticationPrincipal Jwt jwt,@PathVariable Long orderId){
        cancelOrderUseCase.cancelByBuyer(orderId,userId(jwt));
        return ResponseEntity.noContent().build();
    }

    private static Long userId(Jwt jwt){
        return Long.valueOf(jwt.getSubject());
    }
    static CheckoutResponse toCheckoutResponse(PlaceOrderUseCase.CheckoutResult result) {
        return new CheckoutResponse(OrderResponse.from(result.order()), result.checkoutUrl());
    }
    static PlaceOrderUseCase.PickupSlotProposal pickupProposal(Instant start,
                                                               Instant end,
                                                               AuthorizedPickupPerson person) {
        if (start == null && end == null && person == null) return null;
        return new PlaceOrderUseCase.PickupSlotProposal(start, end, person);
    }

    public record CheckoutResponse(OrderResponse order, String checkoutUrl) {}
    public record CheckoutUrlResponse(String checkoutUrl) {}
    public record DirectOrderRequest(Long eventId,Set<Long> itemIds, Instant pickupStartTime,
                                     Instant pickupEndTime, AuthorizedPickupPerson authorizedPerson){}
    public record TakeItAllRequest(Long eventId, Instant pickupStartTime, Instant pickupEndTime,
                                   AuthorizedPickupPerson authorizedPerson){}
}