package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.transaction.Order.DeliveryMethod;

import java.util.Set;

public interface CartAndCheckoutUseCase {

    record AddToCartCommand(
            Long buyerId,
            Long itemId
    ) {}

    record CheckoutCartCommand(
            Long buyerId,
            Long targetEventId,
            DeliveryMethod deliveryMethod
    ) {}

    void addItemToCart(AddToCartCommand command);

    Order checkout(CheckoutCartCommand command);
}