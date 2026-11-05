package com.muvix.application.port.out.persistence;

import com.muvix.domain.model.catalogue.Batch;
import com.muvix.domain.model.catalogue.Product;

import java.math.BigDecimal;

/**
 * Puerto de Salida para proyectar cambios de escritura (MySQL) hacia la vista de lectura (Cassandra).
 */
public interface CatalogProjectionPort {

    void projectProductToCatalog(Product product, String zone, Long boxId);

    void projectBatchToCatalog(Batch batch, String zone);

    void updateDynamicPriceInCatalog(Long itemId, String zone, BigDecimal newPrice);

    void removeFromCatalog(Long itemId, String zone);
}
