package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.transaction.Order.DeliveryMethod;

import java.util.Set;
import java.util.List;
import com.muvix.domain.model.catalogue.SellableItem;

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

    record CheckoutResult(Order order, String checkoutUrl) {}

    void addItemToCart(AddToCartCommand command);
    void removeItemFromCart(Long buyerId, Long itemId);

    List<SellableItem> getCartItems(Long buyerId);

    Order checkout(CheckoutCartCommand command);

    CheckoutResult checkoutWithPayment(CheckoutCartCommand command);
}
