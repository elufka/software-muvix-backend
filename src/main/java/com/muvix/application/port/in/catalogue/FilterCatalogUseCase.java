package com.muvix.application.port.in.catalogue;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;

import java.math.BigDecimal;
import java.util.List;

public interface FilterCatalogUseCase {

    enum SortOption {
        TITLE_ASC,
        TITLE_DESC,
        PRICE_ASC,
        PRICE_DESC
    }

    record FilterCatalogQuery(
            String zone,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            SortOption sortBy
    ) {}

    List<CatalogItemCassandraEntity> filterCatalog(FilterCatalogQuery query);
}