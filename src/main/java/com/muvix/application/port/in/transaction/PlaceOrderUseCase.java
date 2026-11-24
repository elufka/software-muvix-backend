package com.muvix.application.port.in.transaction;

import com.muvix.domain.model.transaction.Order;
import com.muvix.domain.model.logistic.AuthorizedPickupPerson;

import java.time.Instant;
import java.util.Set;

public interface PlaceOrderUseCase {

    record CheckoutResult(Order order, String checkoutUrl) {}

    record PickupSlotProposal(Instant startTime, Instant endTime, AuthorizedPickupPerson authorizedPerson) {}

    record DirectPurchaseCommand(Long buyerId, Long eventId, Set<Long> itemIds, PickupSlotProposal pickupSlot) {
        public DirectPurchaseCommand(Long buyerId, Long eventId, Set<Long> itemIds) {
            this(buyerId, eventId, itemIds, null);
        }
    }

    record TakeItAllPurchaseCommand(Long buyerId, Long eventId, PickupSlotProposal pickupSlot) {
        public TakeItAllPurchaseCommand(Long buyerId, Long eventId) { this(buyerId, eventId, null); }
    }

    record AcceptOfferPurchaseCommand(Long actorId, Long offerId, PickupSlotProposal pickupSlot) {
        public AcceptOfferPurchaseCommand(Long actorId, Long offerId) { this(actorId, offerId, null); }
    }

    CheckoutResult executeDirectPurchase(DirectPurchaseCommand command);
    CheckoutResult executeTakeItAllPurchase(TakeItAllPurchaseCommand command);
    CheckoutResult executeOfferPurchase(AcceptOfferPurchaseCommand command);
}