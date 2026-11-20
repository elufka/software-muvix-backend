package com.muvix.application.port.out.persistence;

import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase;
import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogPage;
import com.muvix.application.port.in.catalogue.SellableItemQueryUseCase.CatalogFilterQuery;

import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Puerto de Salida para persistencia transaccional de Productos y Lotes en MySQL.
 */
public interface SellableItemRepositoryPort {

    SellableItem save(SellableItem item);

    Optional<SellableItem> findById(Long id);

    List<Product> findProductsByIds(Set<Long> productIds);

    List<SellableItem> findBySellerId(Long sellerId);

    CatalogPage searchPublicCatalog(CatalogFilterQuery query);

}
