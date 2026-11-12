package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "orders")
public class OrderJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    @Column(name = "item_id")
    private Set<Long> itemIds = new HashSet<>();

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OriginJpa origin;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivery_method", nullable = false)
    private DeliveryMethodJpa deliveryMethod;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusJpa status;

    @Column(name = "slot_id")
    private Long slotId;

    // Embedded Value Objects
    @Embedded
    private AuthorizedPickupPersonEmbeddable authorizedPerson;

    @Column(name = "pickup_pin", length = 6)
    private String pickupPin;

    @Column(name = "delivered_at")
    private Instant deliveredAt;

    public enum OriginJpa { DIRECT_PURCHASE, ACCEPTED_OFFER, TAKE_IT_ALL }
    public enum DeliveryMethodJpa { PICKUP, SHIPPING }
    public enum StatusJpa { PENDING_SLOT, SLOT_CONFIRMED, DELIVERED, CANCELLED }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }
    public Set<Long> getItemIds() { return itemIds; }
    public void setItemIds(Set<Long> itemIds) { this.itemIds = itemIds; }
    public BigDecimal getTotal() { return total; }
    public void setTotal(BigDecimal total) { this.total = total; }
    public OriginJpa getOrigin() { return origin; }
    public void setOrigin(OriginJpa origin) { this.origin = origin; }
    public DeliveryMethodJpa getDeliveryMethod() { return deliveryMethod; }
    public void setDeliveryMethod(DeliveryMethodJpa deliveryMethod) { this.deliveryMethod = deliveryMethod; }
    public StatusJpa getStatus() { return status; }
    public void setStatus(StatusJpa status) { this.status = status; }
    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }
    public AuthorizedPickupPersonEmbeddable getAuthorizedPerson() { return authorizedPerson; }
    public void setAuthorizedPerson(AuthorizedPickupPersonEmbeddable authorizedPerson) { this.authorizedPerson = authorizedPerson; }
    public String getPickupPin() { return pickupPin; }
    public void setPickupPin(String pickupPin) { this.pickupPin = pickupPin; }
    public Instant getDeliveredAt() { return deliveredAt; }
    public void setDeliveredAt(Instant deliveredAt) { this.deliveredAt = deliveredAt; }
}

@Embeddable
class AuthorizedPickupPersonEmbeddable {
    @Column(name = "authorized_person_name")
    private String fullName;
    @Column(name = "authorized_person_doc")
    private String documentId;
    @Column(name = "authorized_person_phone")
    private String phone;

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }
    public String getDocumentId() { return documentId; }
    public void setDocumentId(String documentId) { this.documentId = documentId; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
