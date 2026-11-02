package com.muvix.domain.model.transaction;

import com.muvix.domain.logistic.AuthorizedPickupPerson;
import com.muvix.domain.logistic.PickupPin;
import com.muvix.domain.model.exception.DomainException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

public class Order {

    public enum Origin { DIRECT_PURCHASE, ACCEPTED_OFFER, TAKE_IT_ALL }
    public enum DeliveryMethod { PICKUP, SHIPPING }
    public enum Status { PENDING_SLOT, SLOT_CONFIRMED, DELIVERED, CANCELLED }

    private final Long id;
    private final Long eventId;
    private final Long buyerId;
    private final Set<Long> itemIds = new HashSet<>();
    private final BigDecimal total;
    private final Origin origin;
    private final DeliveryMethod deliveryMethod;
    private Long slotId;
    private Status status;

    private AuthorizedPickupPerson authorizedPickupPerson;
    private PickupPin pickupPin;
    private Instant deliveredAt;

    public Order(Long id, Long eventId, Long buyerId, Set<Long> itemIds, BigDecimal total,
                 Origin origin, DeliveryMethod deliveryMethod, Long slotId, Status status) {
        if (eventId == null || buyerId == null) throw new DomainException("Event and Buyer are required");
        if (itemIds == null || itemIds.isEmpty()) throw new DomainException("Order must contain at least one item");
        if (total == null || total.signum() <= 0) throw new DomainException("Invalid total amount");

        this.id = id;
        this.eventId = eventId;
        this.buyerId = buyerId;
        this.itemIds.addAll(itemIds);
        this.total = total;
        this.origin = origin;
        this.deliveryMethod = deliveryMethod == null ? DeliveryMethod.PICKUP : deliveryMethod;
        this.slotId = slotId;
        this.status = status == null ? Status.PENDING_SLOT : status;
    }

    public void setAuthorizedPickupPerson(AuthorizedPickupPerson person) {
        if (deliveryMethod != DeliveryMethod.PICKUP) {
            throw new DomainException("Authorized person is only applicable for pickup orders");
        }
        this.authorizedPickupPerson = person;
        if (this.pickupPin == null) {
            this.pickupPin = PickupPin.generate();
        }
    }

    public void confirmSlot(Long slotId) {
        if (status != Status.PENDING_SLOT) throw new DomainException("Order state does not allow slot selection");
        if (deliveryMethod == DeliveryMethod.PICKUP && authorizedPickupPerson == null) {
            throw new DomainException("Authorized pickup person must be provided before confirming slot");
        }
        this.slotId = slotId;
        this.status = Status.SLOT_CONFIRMED;
    }

    public void completePickup(String providedPin) {
        if (status != Status.SLOT_CONFIRMED) throw new DomainException("Order is not in slot confirmed status");
        if (pickupPin == null || !pickupPin.validate(providedPin)) {
            throw new DomainException("Invalid verification PIN for pickup");
        }
        this.status = Status.DELIVERED;
        this.deliveredAt = Instant.now();
    }

    public boolean canViewExactAddress() {
        return status == Status.SLOT_CONFIRMED || status == Status.DELIVERED;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getBuyerId() { return buyerId; }
    public Set<Long> getItemIds() { return Set.copyOf(itemIds); }
    public BigDecimal getTotal() { return total; }
    public Origin getOrigin() { return origin; }
    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public Long getSlotId() { return slotId; }
    public Status getStatus() { return status; }
    public AuthorizedPickupPerson getAuthorizedPickupPerson() { return authorizedPickupPerson; }
    public PickupPin getPickupPin() { return pickupPin; }
    public Instant getDeliveredAt() { return deliveredAt; }
}
