package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase;
import com.muvix.application.port.out.persistence.SellableItemRepositoryPort;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.domain.model.exception.DomainException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class SellableItemQueryService implements SellableItemQueryUseCase {

    private final SellableItemRepositoryPort itemRepositoryPort;

    public SellableItemQueryService(SellableItemRepositoryPort itemRepositoryPort) {
        this.itemRepositoryPort = itemRepositoryPort;
    }

    @Override
    public SellableItem getOwnedById(Long itemId, Long sellerId) {
        SellableItem item = itemRepositoryPort.findById(itemId)
                .orElseThrow(() -> new DomainException("Sellable item not found"));

        if (!item.getSellerId().equals(sellerId)) {
            throw new DomainException("Only the seller can view this item");
        }
        return item;
    }

    @Override
    public List<SellableItem> getBySellerId(Long sellerId) {
        return itemRepositoryPort.findBySellerId(sellerId).stream()
                .filter(item -> item.getState() != SellableItem.State.SOFT_DELETED && item.isPublic())
                .toList();
    }


    @Override
    public List<SellableItem> getByStorageItemBySellerId(Long sellerId) {
        return itemRepositoryPort.findBySellerId(sellerId).stream()
                .filter(item -> item.getState() == SellableItem.State.PAUSED  && !item.isPublic())
                .toList();
    }

    @Override
    public SellableItem getPublicById(Long itemId) {
        SellableItem item = itemRepositoryPort.findById(itemId)
                .orElseThrow(() -> new DomainException("Sellable item not found"));

        return item;
    }

    @Override
    public CatalogPage searchPublicCatalog(CatalogFilterQuery query) {
        return itemRepositoryPort.searchPublicCatalog(query);
    }
}
