package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.FilterCatalogUseCase;
import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Service
public class FilterCatalogService implements FilterCatalogUseCase {

    private final CatalogQueryPort catalogQueryPort;

    public FilterCatalogService(CatalogQueryPort catalogQueryPort) {
        this.catalogQueryPort = catalogQueryPort;
    }

    @Override
    public List<CatalogItemCassandraEntity> filterCatalog(FilterCatalogQuery query) {
        // 1. Lectura por partición primaria de zona
        List<CatalogItemCassandraEntity> items = (query.category() != null && !query.category().isBlank())
                ? catalogQueryPort.findByZoneAndCategory(query.zone(), query.category())
                : catalogQueryPort.findByZone(query.zone());

        Stream<CatalogItemCassandraEntity> stream = items.stream();

        // 2. Filtro en memoria por rangos de precio
        if (query.minPrice() != null) {
            stream = stream.filter(item -> item.getCurrentDynamicPrice().compareTo(query.minPrice()) >= 0);
        }
        if (query.maxPrice() != null) {
            stream = stream.filter(item -> item.getCurrentDynamicPrice().compareTo(query.maxPrice()) <= 0);
        }

        // 3. Ordenamiento (Alfabético / Precio)
        SortOption sort = query.sortBy() != null ? query.sortBy() : SortOption.TITLE_ASC;
        Comparator<CatalogItemCassandraEntity> comparator = switch (sort) {
            case TITLE_ASC -> Comparator.comparing(CatalogItemCassandraEntity::getTitle, String.CASE_INSENSITIVE_ORDER);
            case TITLE_DESC -> Comparator.comparing(CatalogItemCassandraEntity::getTitle, String.CASE_INSENSITIVE_ORDER).reversed();
            case PRICE_ASC -> Comparator.comparing(CatalogItemCassandraEntity::getCurrentDynamicPrice);
            case PRICE_DESC -> Comparator.comparing(CatalogItemCassandraEntity::getCurrentDynamicPrice).reversed();
        };

        return stream.sorted(comparator).toList();
    }
}