package com.muvix.application.port.in.catalogue;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;

import java.math.BigDecimal;
import java.util.List;

public interface SearchCatalogUseCase {

    record CatalogFilterCommand(
            String zone,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String sortBy // "PRICE_ASC", "PRICE_DESC", "TITLE_ASC"
    ) {}

    record MapViewResponse(
            String zone,
            List<CatalogItemCassandraEntity> itemsInZone,
            List<PublicBoxCassandraEntity> boxesInZone
    ) {}

    List<CatalogItemCassandraEntity> filterCatalog(CatalogFilterCommand command);

    MapViewResponse getItemsNearZone(String currentZone, String destinationZone);
}