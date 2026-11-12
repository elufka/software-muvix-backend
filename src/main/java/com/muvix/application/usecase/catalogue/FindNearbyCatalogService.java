import com.muvix.application.port.in.catalogue.FindNearbyCatalogUseCase;
import com.muvix.application.port.out.location.LocationGatewayPort;
import com.muvix.application.port.out.persistence.CatalogQueryPort;
import com.muvix.domain.model.logistic.ZoneDetail;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.CatalogItemCassandraEntity;
import com.muvix.infrastructure.adapters.out.persistence.cassandra.entity.PublicBoxCassandraEntity;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class FindNearbyCatalogService implements FindNearbyCatalogUseCase {

    private final CatalogQueryPort catalogQueryPort;
    private final LocationGatewayPort locationGatewayPort;

    public FindNearbyCatalogService(
            CatalogQueryPort catalogQueryPort,
            LocationGatewayPort locationGatewayPort
    ) {
        this.catalogQueryPort = catalogQueryPort;
        this.locationGatewayPort = locationGatewayPort;
    }

    @Override
    public NearbyCatalogResponse findNearbyByCoordinates(GeoLocationQuery query) {

        ZoneDetail baseZone = locationGatewayPort.resolveZoneFromCoordinates(
                query.latitude(),
                query.longitude()
        );

        List<ZoneDetail> zonesToSearch = new ArrayList<>(
                locationGatewayPort.findNearbyZones(
                        query.latitude(),
                        query.longitude(),
                        query.radiusKm()
                )
        );

        boolean baseZoneIncluded = zonesToSearch.stream()
                .anyMatch(zone -> zone.zoneId().equals(baseZone.zoneId()));

        if (!baseZoneIncluded) {
            zonesToSearch.add(baseZone);
        }

        List<CatalogItemCassandraEntity> allItems = new ArrayList<>();
        List<PublicBoxCassandraEntity> allBoxes = new ArrayList<>();

        for (ZoneDetail zone : zonesToSearch) {
            allItems.addAll(catalogQueryPort.findByZone(zone.zoneId()));
            allBoxes.addAll(catalogQueryPort.findBoxesByZone(zone.zoneId()));
        }

        return new NearbyCatalogResponse(
                baseZone.zoneId(),
                allItems,
                allBoxes
        );
    }

    @Override
    public DualZoneMapResponse findNearbyForRelocation(
            GeoLocationQuery origin,
            GeoLocationQuery destination
    ) {
        NearbyCatalogResponse originCatalog =
                findNearbyByCoordinates(origin);

        NearbyCatalogResponse destinationCatalog =
                destination != null
                        ? findNearbyByCoordinates(destination)
                        : null;

        return new DualZoneMapResponse(
                originCatalog,
                destinationCatalog
        );
    }
}
