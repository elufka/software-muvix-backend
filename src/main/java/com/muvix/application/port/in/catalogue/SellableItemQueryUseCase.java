package com.muvix.application.port.in.catalogue;

import com.muvix.domain.model.catalogue.SellableItem;
import com.muvix.infrastructure.adapters.out.persistence.mysql.entity.enums.ProductCondition;

import java.math.BigDecimal;
import java.util.List;

public interface SellableItemQueryUseCase {
    enum SortOption {
        NEWEST,
        PRICE_ASC,
        PRICE_DESC,
        TITLE_ASC,
        TITLE_DESC
    }

    record CatalogFilterQuery(
            String text,          // Búsqueda opcional por título o descripción
            String zone,          // Filtro opcional por zona (ej. "Mar del Plata")
            String category,      // Filtro opcional por categoría
            ProductCondition condition,
            BigDecimal minPrice,  // Precio mínimo opcional
            BigDecimal maxPrice,  // Precio máximo opcional
            SortOption sortBy,    // Ordenamiento (por defecto NEWEST)
            int page,             // Número de página (empezando en 0)
            int size              // Cantidad de ítems por página (ej. 20)
    )
    {
        public CatalogFilterQuery {
            if (sortBy == null) sortBy = SortOption.NEWEST;
            if (page < 0) page = 0;
            if (size <= 0 || size > 100) size = 20; // Evita que pidan size=999999
        }
    }

    record CatalogPage(
            List<SellableItem> items,
            int page,
            int size,
            long totalElements,
            int totalPages
    ) {}

    SellableItem getOwnedById(Long itemId, Long sellerId);

    List<SellableItem> getBySellerId(Long sellerId);

    List<SellableItem> getByStorageItemBySellerId(Long sellerId); // Agarramos las publicaciones con price 0 (storage items) del user autenticado

    SellableItem getPublicById(Long itemId);

    CatalogPage searchPublicCatalog(CatalogFilterQuery query);

}
