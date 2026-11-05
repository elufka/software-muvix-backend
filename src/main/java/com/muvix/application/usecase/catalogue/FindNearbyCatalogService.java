package com.muvix.application.usecase.catalogue;

import com.muvix.application.port.in.catalogue.FindNearbyCatalogUseCase;
import com.muvix.application.port.out.location.LocationGatewayPort;
import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FindNearbyCatalogService implements FindNearbyCatalogUseCase {

    private final CatalogQueryPort catalogQueryPort;
    private final LocationGatewayPort locationGatewayPort;

    public FindNearbyCatalogService(CatalogQueryPort catalogQueryPort,
                                    LocationGatewayPort locationGatewayPort) {
        this.catalogQueryPort = catalogQueryPort;
        this.locationGatewayPort = locationGatewayPort;
    }

    @Override
    public NearbyCatalogResponse findNearbyByCoordinates(GeoLocationQuery query) {
        // 1. Invocación al servicio externo de geolocalización para mapear GPS -> Zona
        String baseZone = locationGatewayPort.resolveZoneFromCoordinates(
                new LocationGatewayPort.GeoCoordinates(query.latitude(), query.longitude())
        );

        // 2. Obtener zonas colindantes
        List<String> zonesToSearch = locationGatewayPort.findNearbyZones(baseZone, query.radiusKm());
        if (!zonesToSearch.contains(baseZone)) {
            zonesToSearch.add(baseZone);
        }

        // 3. Consultar publicaciones y Boxes desnormalizadas en Cassandra para cada zona sin exponer direcciones exactas
        List<CatalogItemCassandraEntity> allItems = new ArrayList<>();
        List<PublicBoxCassandraEntity> allBoxes = new ArrayList<>();

        for (String zone : zonesToSearch) {
            allItems.addAll(catalogQueryPort.findByZone(zone));
            allBoxes.addAll(catalogQueryPort.findBoxesByZone(zone));
        }

        return new NearbyCatalogResponse(baseZone, allItems, allBoxes);
    }

    @Override
    public DualZoneMapResponse findNearbyForRelocation(GeoLocationQuery origin, GeoLocationQuery destination) {
        NearbyCatalogResponse originCatalog = findNearbyByCoordinates(origin);
        NearbyCatalogResponse destinationCatalog = destination != null ? findNearbyByCoordinates(destination) : null;

        return new DualZoneMapResponse(originCatalog, destinationCatalog);
    }
}