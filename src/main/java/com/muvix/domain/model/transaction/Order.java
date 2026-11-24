package com.muvix.domain.model.transaction;

import com.muvix.domain.model.logistic.AuthorizedPickupPerson;
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
    private final String buyerName;
    private final String sellerName;
    private final Set<Long> itemIds = new HashSet<>();
    private final Set<Long> sourceBatchIds = new HashSet<>();
    private final BigDecimal total;
    private final Origin origin;
    private final DeliveryMethod deliveryMethod;
    private Instant createdAt;
    private Long slotId;
    private Status status;

    private AuthorizedPickupPerson authorizedPickupPerson;
    private String pickupPinHash;
    private Instant deliveredAt;

    public Order(Long id, Long eventId, Long buyerId, Set<Long> itemIds, BigDecimal total,
                 Origin origin, DeliveryMethod deliveryMethod, Long slotId, Status status) {
        this(id, eventId, buyerId, null, null, itemIds, total, origin, deliveryMethod, slotId, status);
    }

    public Order(Long id, Long eventId, Long buyerId, String buyerName, String sellerName,
                 Set<Long> itemIds, BigDecimal total, Origin origin, DeliveryMethod deliveryMethod,
                 Long slotId, Status status) {
        if (buyerId == null) throw new DomainException("Buyer is required");
        if (itemIds == null || itemIds.isEmpty()) throw new DomainException("Order must contain at least one item");
        if (total == null || total.signum() <= 0) throw new DomainException("Invalid total amount");

        this.id = id;
        this.eventId = eventId;
        this.buyerId = buyerId;
        this.buyerName = buyerName;
        this.sellerName = sellerName;
        this.itemIds.addAll(itemIds);
        this.total = total;
        this.origin = origin;
        this.deliveryMethod = deliveryMethod == null ? DeliveryMethod.PICKUP : deliveryMethod;
        this.slotId = slotId;
        this.status = status == null ? Status.PENDING_SLOT : status;
        this.createdAt = Instant.now();
    }

    public static Order restore(Long id, Long eventId, Long buyerId, Set<Long> itemIds, BigDecimal total,
                                Origin origin, DeliveryMethod deliveryMethod, Long slotId, Status status,
                                AuthorizedPickupPerson authorizedPickupPerson, String pickupPinHash, Instant deliveredAt) {
        return restore(id, eventId, buyerId, null, null, itemIds, total, origin, deliveryMethod, slotId,
                status, authorizedPickupPerson, pickupPinHash, deliveredAt);
    }

    public static Order restore(Long id, Long eventId, Long buyerId, String buyerName, String sellerName,
                                Set<Long> itemIds, BigDecimal total, Origin origin, DeliveryMethod deliveryMethod,
                                Long slotId, Status status, AuthorizedPickupPerson authorizedPickupPerson,
                                String pickupPinHash, Instant deliveredAt) {
        Order order = new Order(id, eventId, buyerId, buyerName, sellerName, itemIds, total, origin,
                deliveryMethod, slotId, status);
        order.authorizedPickupPerson = authorizedPickupPerson;
        order.pickupPinHash = pickupPinHash;
        order.deliveredAt = deliveredAt;
        return order;
    }

    public static Order restore(Long id,Long eventId,Long buyerId,String buyerName,String sellerName,Set<Long> itemIds,
                                Set<Long> sourceBatchIds,BigDecimal total,Origin origin,DeliveryMethod deliveryMethod,
                                Long slotId,Status status,AuthorizedPickupPerson authorizedPickupPerson,String pickupPinHash,
                                Instant deliveredAt,Instant createdAt){

        Order order=restore(id,eventId,buyerId,buyerName,sellerName,itemIds,sourceBatchIds,total,origin,deliveryMethod,slotId,status,authorizedPickupPerson,pickupPinHash,deliveredAt);

        if(createdAt!=null)order.createdAt=createdAt;
        return order;
    }

    public static Order restore(Long id, Long eventId, Long buyerId, Set<Long> itemIds, Set<Long> sourceBatchIds,
                                BigDecimal total, Origin origin, DeliveryMethod deliveryMethod, Long slotId,
                                Status status, AuthorizedPickupPerson authorizedPickupPerson, String pickupPinHash,
                                Instant deliveredAt) {
        return restore(id, eventId, buyerId, null, null, itemIds, sourceBatchIds, total, origin,
                deliveryMethod, slotId, status, authorizedPickupPerson, pickupPinHash, deliveredAt);
    }

    public static Order restore(Long id, Long eventId, Long buyerId, String buyerName, String sellerName,
                                Set<Long> itemIds, Set<Long> sourceBatchIds, BigDecimal total, Origin origin,
                                DeliveryMethod deliveryMethod, Long slotId, Status status,
                                AuthorizedPickupPerson authorizedPickupPerson, String pickupPinHash,
                                Instant deliveredAt) {
        Order order = restore(id,eventId,buyerId,buyerName,sellerName,itemIds,total,origin,deliveryMethod,slotId,status,
                authorizedPickupPerson,pickupPinHash,deliveredAt);
        if (sourceBatchIds != null) order.sourceBatchIds.addAll(sourceBatchIds);
        return order;
    }

    public void setAuthorizedPickupPerson(AuthorizedPickupPerson person) {
        if (deliveryMethod != DeliveryMethod.PICKUP) {
            throw new DomainException("Authorized person is only applicable for pickup orders");
        }
        if (person == null) throw new DomainException("Authorized pickup person is required");
        this.authorizedPickupPerson = person;
    }

    public void assignPickupPinHash(String encodedPin) {
        if (authorizedPickupPerson == null) throw new DomainException("Authorized pickup person is required before issuing a PIN");
        if (encodedPin == null || encodedPin.isBlank()) throw new DomainException("Encoded pickup PIN is required");
        if (status != Status.SLOT_CONFIRMED) throw new DomainException("Pickup PIN can only be issued after the slot is confirmed");
        this.pickupPinHash = encodedPin;
    }

    public void confirmSlot(Long slotId) {
        if (status != Status.PENDING_SLOT) throw new DomainException("Order state does not allow slot selection");
        if (deliveryMethod == DeliveryMethod.PICKUP && authorizedPickupPerson == null) {
            throw new DomainException("Authorized pickup person must be provided before confirming slot");
        }
        this.slotId = slotId;
        this.status = Status.SLOT_CONFIRMED;
    }

    public void attachPickupSlotProposal(Long slotId) {
        if (status != Status.PENDING_SLOT) throw new DomainException("Order is not awaiting a pickup slot");
        if (deliveryMethod != DeliveryMethod.PICKUP) throw new DomainException("Order is not a pickup order");
        if (slotId == null) throw new DomainException("Pickup slot ID is required");
        this.slotId = slotId;
    }

    public void cancelBeforeSellerPickup() {
        if (status != Status.PENDING_SLOT) throw new DomainException("Order can no longer be cancelled by the buyer");
        status = Status.CANCELLED;
    }

    public void recordSourceBatchIds(Set<Long> batchIds) {
        if (status != Status.PENDING_SLOT) throw new DomainException("Source batches can only be recorded before pickup confirmation");
        if (batchIds != null) sourceBatchIds.addAll(batchIds);
    }

    public void completePickup() {
        if (status != Status.SLOT_CONFIRMED) throw new DomainException("Order is not in slot confirmed status");
        if (pickupPinHash == null) throw new DomainException("Buyer must request a pickup PIN before delivery");
        this.status = Status.DELIVERED;
        this.deliveredAt = Instant.now();
        this.pickupPinHash = null;
    }

    public boolean canViewExactAddress() {
        return status == Status.SLOT_CONFIRMED || status == Status.DELIVERED;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public Long getBuyerId() { return buyerId; }
    public String getBuyerName() { return buyerName; }
    public String getSellerName() { return sellerName; }
    public Set<Long> getItemIds() { return Set.copyOf(itemIds); }
    public Set<Long> getSourceBatchIds() { return Set.copyOf(sourceBatchIds); }
    public BigDecimal getTotal() { return total; }
    public Origin getOrigin() { return origin; }
    public DeliveryMethod getDeliveryMethod() { return deliveryMethod; }
    public Long getSlotId() { return slotId; }
    public Status getStatus() { return status; }
    public AuthorizedPickupPerson getAuthorizedPickupPerson() { return authorizedPickupPerson; }
    public String getPickupPinHash() { return pickupPinHash; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public Instant getCreatedAt() { return createdAt; }
}