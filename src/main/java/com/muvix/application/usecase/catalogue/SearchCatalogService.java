package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.SearchCatalogUseCase;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.CatalogItemCassandraRepository;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.repository.PublicBoxCassandraRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchCatalogService implements SearchCatalogUseCase {

    private final CatalogItemCassandraRepository catalogRepository;
    private final PublicBoxCassandraRepository boxRepository;

    public SearchCatalogService(CatalogItemCassandraRepository catalogRepository,
                                PublicBoxCassandraRepository boxRepository) {
        this.catalogRepository = catalogRepository;
        this.boxRepository = boxRepository;
    }

    @Override
    public List<CatalogItemCassandraEntity> filterCatalog(CatalogFilterCommand command) {
        // Lectura directa en Cassandra por partición de Zona
        List<CatalogItemCassandraEntity> items = catalogRepository.findByZone(command.zone());

        return items.stream()
                .filter(item -> command.category() == null || command.category().equalsIgnoreCase(item.getCategory()))
                .filter(item -> command.minPrice() == null || item.getCurrentDynamicPrice().compareTo(command.minPrice()) >= 0)
                .filter(item -> command.maxPrice() == null || item.getCurrentDynamicPrice().compareTo(command.maxPrice()) <= 0)
                .sorted((i1, i2) -> {
                    if ("PRICE_ASC".equalsIgnoreCase(command.sortBy())) {
                        return i1.getCurrentDynamicPrice().compareTo(i2.getCurrentDynamicPrice());
                    } else if ("PRICE_DESC".equalsIgnoreCase(command.sortBy())) {
                        return i2.getCurrentDynamicPrice().compareTo(i1.getCurrentDynamicPrice());
                    } else {
                        return i1.getTitle().compareToIgnoreCase(i2.getTitle()); // Alfabetico por defecto
                    }
                })
                .toList();
    }

    @Override
    public MapViewResponse getItemsNearZone(String currentZone, String destinationZone) {
        List<CatalogItemCassandraEntity> combinedItems = new ArrayList<>();
        List<PublicBoxCassandraEntity> combinedBoxes = new ArrayList<>();

        // Consulta desnormalizada por Zona Actual (sin mostrar dirección exacta)
        if (currentZone != null && !currentZone.isBlank()) {
            combinedItems.addAll(catalogRepository.findByZone(currentZone));
            combinedBoxes.addAll(boxRepository.findByZone(currentZone));
        }

        // Consulta por Zona de Destino (para mudanzas en curso)
        if (destinationZone != null && !destinationZone.isBlank() && !destinationZone.equalsIgnoreCase(currentZone)) {
            combinedItems.addAll(catalogRepository.findByZone(destinationZone));
            combinedBoxes.addAll(boxRepository.findByZone(destinationZone));
        }

        return new MapViewResponse(currentZone, combinedItems, combinedBoxes);
    }
}