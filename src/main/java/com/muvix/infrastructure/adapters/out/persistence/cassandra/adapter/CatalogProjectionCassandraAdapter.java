package com.muvix.infrastructure.adapters.out.persistence.cassandra.adapter;

import com.muvix.application.port.out.persistence.CatalogProjectionPort;
import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.exception.DomainException;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.CatalogItemCassandraRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Component
public class CatalogProjectionCassandraAdapter implements CatalogProjectionPort {

    private final CatalogItemCassandraRepository repository;
    private final Clock clock;

    public CatalogProjectionCassandraAdapter(CatalogItemCassandraRepository repository, Clock clock) {
        this.repository = repository;
        this.clock = clock;
    }

    @Override
    public void projectProductToCatalog(Product product, String zone, Long boxId) {
        requireZone(zone);
        CatalogItemCassandraEntity entity = findExisting(product.getId(), zone)
                .orElseGet(CatalogItemCassandraEntity::new);
        entity.setZone(zone);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(Instant.now(clock));
        entity.setItemId(product.getId());
        entity.setSellerId(product.getSellerId());
        entity.setTitle(product.getTitle());
        entity.setDescription(product.getDescription());
        entity.setOriginalPrice(product.getPrice());
        entity.setCurrentDynamicPrice(product.getPrice());
        entity.setItemType("PRODUCT");
        entity.setCategory(product.getCategory());
        entity.setMainImageUrl(product.getImages().isEmpty() ? null : product.getImages().getFirst());
        entity.setTakeItAllEligible(false);
        entity.setBoxId(boxId);
        repository.save(entity);
    }

    @Override
    public void projectBatchToCatalog(Batch batch, String zone) {
        requireZone(zone);
        CatalogItemCassandraEntity entity = findExisting(batch.getId(), zone)
                .orElseGet(CatalogItemCassandraEntity::new);
        entity.setZone(zone);
        if (entity.getCreatedAt() == null) entity.setCreatedAt(Instant.now(clock));
        entity.setItemId(batch.getId());
        entity.setSellerId(batch.getSellerId());
        entity.setTitle(batch.getTitle());
        entity.setDescription(batch.getDescription());
        entity.setOriginalPrice(batch.getPrice());
        entity.setCurrentDynamicPrice(batch.getPrice());
        entity.setItemType("BATCH");
        entity.setCategory(null);
        entity.setMainImageUrl(null);
        entity.setTakeItAllEligible(false);
        entity.setBoxId(null);
        repository.save(entity);
    }

    @Override
    public void updateDynamicPriceInCatalog(Long itemId, String zone, BigDecimal newPrice) {
        if (newPrice == null || newPrice.signum() < 0) throw new DomainException("Dynamic price cannot be negative");
        findExisting(itemId, zone).ifPresent(entity -> {
            entity.setCurrentDynamicPrice(newPrice);
            repository.save(entity);
        });
    }

    @Override
    public void removeFromCatalog(Long itemId, String zone) {
        findExisting(itemId, zone).ifPresent(repository::delete);
    }

    private java.util.Optional<CatalogItemCassandraEntity> findExisting(Long itemId, String zone) {
        return repository.findByZone(zone).stream()
                .filter(item -> itemId.equals(item.getItemId()))
                .findFirst();
    }

    private static void requireZone(String zone) {
        if (zone == null || zone.isBlank()) throw new DomainException("Catalog zone is required");
    }
}
