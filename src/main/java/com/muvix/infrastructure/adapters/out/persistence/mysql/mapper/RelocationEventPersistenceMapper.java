package com.muvix.infrastructure.adapters.out.persistence.mysql.mapper;

import com.muvix.domain.model.logistic.Location;
import com.muvix.domain.model.management.DiscountRule;
import com.muvix.domain.model.management.FullRelocationEvent;
import com.muvix.domain.model.management.PurchaseEvent;
import com.muvix.domain.model.management.RelocationEvent;
import com.muvix.domain.model.management.SaleEvent;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.DiscountRuleJpaValue;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.RelocationEventJpaEntity;

import java.util.List;

public final class RelocationEventPersistenceMapper {

    private RelocationEventPersistenceMapper() {}

    public static RelocationEvent toDomain(RelocationEventJpaEntity entity) {
        if (entity == null) return null;

        List<DiscountRule> rules = entity.getDiscountRules().stream()
                .map(rule -> new DiscountRule(rule.getDaysLeft(), rule.getPercentage()))
                .toList();
        RelocationEvent.Status status = RelocationEvent.Status.valueOf(entity.getStatus().name());

        return switch (entity.getEventType()) {
            case SALE -> new SaleEvent(
                    entity.getId(), entity.getUserId(), entity.getTitle(),
                    location(entity.getOriginZone(), entity.getOriginExactAddress()),
                    entity.getSaleDeadline(), entity.getTakeItAllPrice(), rules,
                    entity.getShareToken(), status, entity.getSharedUserIds());
            case PURCHASE -> new PurchaseEvent(
                    entity.getId(), entity.getUserId(), entity.getTitle(),
                    location(entity.getDestinationZone(), entity.getDestinationExactAddress()),
                    entity.getArrivalDeliveryDate(), entity.getShareToken(), status,
                    entity.getSharedUserIds());
            case FULL_RELOCATION -> new FullRelocationEvent(
                    entity.getId(), entity.getUserId(), entity.getTitle(),
                    location(entity.getOriginZone(), entity.getOriginExactAddress()),
                    location(entity.getDestinationZone(), entity.getDestinationExactAddress()),
                    entity.getSaleDeadline(), entity.getArrivalDeliveryDate(),
                    entity.getTakeItAllPrice(), rules, entity.getShareToken(), status,
                    entity.getSharedUserIds());
        };
    }

    public static RelocationEventJpaEntity toEntity(
            RelocationEvent event,
            RelocationEventJpaEntity existing
    ) {
        if (event == null) return null;

        RelocationEventJpaEntity entity = existing == null ? new RelocationEventJpaEntity() : existing;
        entity.setId(event.getId());
        entity.setUserId(event.getUserId());
        entity.setTitle(event.getTitle());
        entity.setShareToken(event.getUniqueShareToken());
        entity.setStatus(RelocationEventJpaEntity.StatusJpa.valueOf(event.getStatus().name()));
        entity.setSharedUserIds(event.getSharedUserIds().stream()
                .collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));

        if (event instanceof SaleEvent sale) {
            entity.setEventType(RelocationEventJpaEntity.EventTypeJpa.SALE);
            setOrigin(entity, sale.getOriginLocation());
            entity.setDestinationZone(null);
            entity.setDestinationExactAddress(null);
            entity.setSaleDeadline(sale.getSaleDeadline());
            entity.setArrivalDeliveryDate(null);
            entity.setTakeItAllPrice(sale.getTakeItAllPrice());
            entity.setDiscountRules(toJpaRules(sale.getDiscountRules()));
        } else if (event instanceof PurchaseEvent purchase) {
            entity.setEventType(RelocationEventJpaEntity.EventTypeJpa.PURCHASE);
            entity.setOriginZone(null);
            entity.setOriginExactAddress(null);
            setDestination(entity, purchase.getDestinationLocation());
            entity.setSaleDeadline(null);
            entity.setArrivalDeliveryDate(purchase.getArrivalDeliveryDate());
            entity.setTakeItAllPrice(null);
            entity.setDiscountRules(List.of());
        } else if (event instanceof FullRelocationEvent full) {
            entity.setEventType(RelocationEventJpaEntity.EventTypeJpa.FULL_RELOCATION);
            setOrigin(entity, full.getOriginLocation());
            setDestination(entity, full.getDestinationLocation());
            entity.setSaleDeadline(full.getSaleDeadline());
            entity.setArrivalDeliveryDate(full.getArrivalDeliveryDate());
            entity.setTakeItAllPrice(full.getTakeItAllPrice());
            entity.setDiscountRules(toJpaRules(full.getDiscountRules()));
        } else {
            throw new IllegalArgumentException("Unsupported relocation event type: " + event.getClass().getName());
        }

        return entity;
    }

    private static Location location(String zone, String exactAddress) {
        return zone == null ? null : new Location(zone, exactAddress);
    }

    private static void setOrigin(RelocationEventJpaEntity entity, Location location) {
        entity.setOriginZone(location.zone());
        entity.setOriginExactAddress(location.exactAddress());
    }

    private static void setDestination(RelocationEventJpaEntity entity, Location location) {
        entity.setDestinationZone(location.zone());
        entity.setDestinationExactAddress(location.exactAddress());
    }

    private static List<DiscountRuleJpaValue> toJpaRules(List<DiscountRule> rules) {
        return rules.stream()
                .map(rule -> new DiscountRuleJpaValue(rule.daysLeft(), rule.percentage()))
                .collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new));
    }
}
