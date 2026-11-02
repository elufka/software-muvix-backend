package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;

import java.math.BigDecimal;
import java.util.Set;

public interface PlaceOrderUseCase {

    record DirectPurchaseCommand(Long buyerId, Long eventId, Set<Long> itemIds) {}

    record TakeItAllPurchaseCommand(Long buyerId, Long eventId) {}

    record AcceptOfferPurchaseCommand(Long buyerId, Long offerId) {}

    Order executeDirectPurchase(DirectPurchaseCommand command);
    Order executeTakeItAllPurchase(TakeItAllPurchaseCommand command);
    Order executeOfferPurchase(AcceptOfferPurchaseCommand command);
}
