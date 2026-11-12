package com.muvix.application.port.in.catalogue;

import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;

import java.util.List;

public interface FindNearbyCatalogUseCase {

    record GeoLocationQuery(double latitude, double longitude, double radiusKm) {}

    record NearbyCatalogResponse(
            String activeZone,
            List<CatalogItemCassandraEntity> nearbyItems,
            List<PublicBoxCassandraEntity> nearbyBoxes
    ) {}

    record DualZoneMapResponse(
            NearbyCatalogResponse currentOriginLocation,
            NearbyCatalogResponse destinationLocation
    ) {}

    NearbyCatalogResponse findNearbyByCoordinates(GeoLocationQuery query);

    DualZoneMapResponse findNearbyForRelocation(GeoLocationQuery origin, GeoLocationQuery destination);
}
