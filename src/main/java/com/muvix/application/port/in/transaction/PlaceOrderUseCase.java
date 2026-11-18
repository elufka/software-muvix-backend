package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;

import java.util.Set;

public interface PlaceOrderUseCase {

    record CheckoutResult(Order order, String checkoutUrl) {}

    record DirectPurchaseCommand(Long buyerId, Long eventId, Set<Long> itemIds) {}

    record TakeItAllPurchaseCommand(Long buyerId, Long eventId) {}

    record AcceptOfferPurchaseCommand(Long sellerId, Long offerId) {}

    CheckoutResult executeDirectPurchase(DirectPurchaseCommand command);
    CheckoutResult executeTakeItAllPurchase(TakeItAllPurchaseCommand command);
    CheckoutResult executeOfferPurchase(AcceptOfferPurchaseCommand command);
}
