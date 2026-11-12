package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.in.transaction.CartAndCheckoutUseCase;
import com.muvix.application.port.in.transaction.PlaceOrderUseCase;
import com.muvix.domain.model.transaction.Order;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<Order> placeDirectOrder(@Valid @RequestBody PlaceOrderUseCase.DirectPurchaseCommand command) {
        Order order = placeOrderUseCase.executeDirectPurchase(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/take-it-all")
    public ResponseEntity<Order> placeTakeItAllOrder(@Valid @RequestBody PlaceOrderUseCase.TakeItAllPurchaseCommand command) {
        Order order = placeOrderUseCase.executeTakeItAllPurchase(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/cart/checkout")
    public ResponseEntity<Order> checkoutCart(@Valid @RequestBody CartAndCheckoutUseCase.CheckoutCartCommand command) {
        Order order = cartAndCheckoutUseCase.checkout(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(order);
    }

    @PostMapping("/cart/items")
    public ResponseEntity<Void> addItemToCart(@RequestParam Long buyerId, @RequestParam Long itemId) {
        cartAndCheckoutUseCase.addItemToCart(new CartAndCheckoutUseCase.AddToCartCommand(buyerId, itemId));
        return ResponseEntity.ok().build();
    }
}