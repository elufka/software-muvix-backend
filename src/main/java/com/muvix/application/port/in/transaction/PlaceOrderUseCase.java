package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;

import java.util.Set;

public interface PlaceOrderUseCase {

    record DirectPurchaseCommand(Long buyerId, Long eventId, Set<Long> itemIds) {}

    record TakeItAllPurchaseCommand(Long buyerId, Long eventId) {}

    record AcceptOfferPurchaseCommand(Long sellerId, Long offerId) {}

    CartAndCheckoutUseCase.CheckoutResult executeDirectPurchase(DirectPurchaseCommand command);
    CartAndCheckoutUseCase.CheckoutResult executeTakeItAllPurchase(TakeItAllPurchaseCommand command);
    CartAndCheckoutUseCase.CheckoutResult executeOfferPurchase(AcceptOfferPurchaseCommand command);
}
