package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.catalogue.Product;
import com.muvix.domain.model.catalogue.SellableItem;

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

    List<SellableItem> findCartItemsByBuyerId(Long buyerId);

    List<SellableItem> findBySellerId(Long sellerId);

    void deleteById(Long id);
}