package com.muvix.infrastructure.adapters.out.persistence.mysql.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "relocation_events")
public class RelocationEventJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 32)
    private EventTypeJpa eventType;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(nullable = false)
    private String title;

    @Column(name = "share_token", nullable = false, unique = true, length = 36)
    private String shareToken;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private StatusJpa status;

    @Column(name = "origin_zone")
    private String originZone;

    @Column(name = "origin_exact_address", columnDefinition = "TEXT")
    private String originExactAddress;

    @Column(name = "destination_zone")
    private String destinationZone;

    @Column(name = "destination_exact_address", columnDefinition = "TEXT")
    private String destinationExactAddress;

    @Column(name = "sale_deadline")
    private Instant saleDeadline;

    @Column(name = "arrival_delivery_date")
    private Instant arrivalDeliveryDate;

    @Column(name = "take_it_all_price", precision = 12, scale = 2)
    private BigDecimal takeItAllPrice;

    @ElementCollection
    @CollectionTable(name = "relocation_event_discount_rules", joinColumns = @JoinColumn(name = "event_id"))
    @OrderColumn(name = "rule_order")
    private List<DiscountRuleJpaValue> discountRules = new ArrayList<>();

    @ElementCollection
    @CollectionTable(name = "relocation_event_shared_users", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "user_id", nullable = false)
    private Set<Long> sharedUserIds = new HashSet<>();

    @ElementCollection
    @CollectionTable(name = "relocation_event_items", joinColumns = @JoinColumn(name = "event_id"))
    @Column(name = "item_id", nullable = false)
    private Set<Long> itemIds = new HashSet<>();

    public enum EventTypeJpa { SALE, PURCHASE, FULL_RELOCATION }
    public enum StatusJpa { DRAFT, ACTIVE, COMPLETED, CANCELLED }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public EventTypeJpa getEventType() { return eventType; }
    public void setEventType(EventTypeJpa eventType) { this.eventType = eventType; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getShareToken() { return shareToken; }
    public void setShareToken(String shareToken) { this.shareToken = shareToken; }
    public StatusJpa getStatus() { return status; }
    public void setStatus(StatusJpa status) { this.status = status; }
    public String getOriginZone() { return originZone; }
    public void setOriginZone(String originZone) { this.originZone = originZone; }
    public String getOriginExactAddress() { return originExactAddress; }
    public void setOriginExactAddress(String originExactAddress) { this.originExactAddress = originExactAddress; }
    public String getDestinationZone() { return destinationZone; }
    public void setDestinationZone(String destinationZone) { this.destinationZone = destinationZone; }
    public String getDestinationExactAddress() { return destinationExactAddress; }
    public void setDestinationExactAddress(String destinationExactAddress) { this.destinationExactAddress = destinationExactAddress; }
    public Instant getSaleDeadline() { return saleDeadline; }
    public void setSaleDeadline(Instant saleDeadline) { this.saleDeadline = saleDeadline; }
    public Instant getArrivalDeliveryDate() { return arrivalDeliveryDate; }
    public void setArrivalDeliveryDate(Instant arrivalDeliveryDate) { this.arrivalDeliveryDate = arrivalDeliveryDate; }
    public BigDecimal getTakeItAllPrice() { return takeItAllPrice; }
    public void setTakeItAllPrice(BigDecimal takeItAllPrice) { this.takeItAllPrice = takeItAllPrice; }
    public List<DiscountRuleJpaValue> getDiscountRules() { return discountRules; }
    public void setDiscountRules(List<DiscountRuleJpaValue> discountRules) { this.discountRules = discountRules; }
    public Set<Long> getSharedUserIds() { return sharedUserIds; }
    public void setSharedUserIds(Set<Long> sharedUserIds) { this.sharedUserIds = sharedUserIds; }
    public Set<Long> getItemIds() { return itemIds; }
    public void setItemIds(Set<Long> itemIds) { this.itemIds = itemIds; }
}
