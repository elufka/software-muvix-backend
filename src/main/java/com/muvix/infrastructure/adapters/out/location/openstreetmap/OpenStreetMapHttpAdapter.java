package com.muvix.infrastructure.adapters.out.location.openstreetmap;

import com.muvix.application.port.out.location.LocationGatewayPort;
import com.muvix.domain.model.logistic.ZoneDetail;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Map;

@Component
public class OpenStreetMapHttpAdapter implements LocationGatewayPort {

    private final RestTemplate restTemplate;

    public OpenStreetMapHttpAdapter(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    @Cacheable(value = "geoZonesCache", key = "#latitude + '-' + #longitude")
    public ZoneDetail resolveZoneFromCoordinates(double latitude, double longitude) {
        String baseUrl = "https://" + "nominatim.openstreetmap.org/reverse";
        String url = String.format(baseUrl + "?format=json&lat=%f&lon=%f&addressdetails=1", latitude, longitude);

        try {
            Map<?, ?> response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("address")) {
                return new ZoneDetail("ZONE_DEFAULT", "Zona General", latitude, longitude, "Desconocido", "Desconocido");
            }

            Map<?, ?> address = (Map<?, ?>) response.get("address");
            String city = address.containsKey("city") ? (String) address.get("city") :
                    address.containsKey("town") ? (String) address.get("town") : "Desconocido";
            String state = address.containsKey("state") ? (String) address.get("state") : "Desconocido";
            String country = address.containsKey("country") ? (String) address.get("country") : "Desconocido";

            String normalizedZoneId = (city + "_" + state).toUpperCase().replaceAll("[^A-Z0-9_]", "_");
            String zoneName = city + ", " + state;

            return new ZoneDetail(normalizedZoneId, zoneName, latitude, longitude, city, country);
        } catch (Exception e) {
            return new ZoneDetail("ZONE_FALLBACK", "Zona Centro", latitude, longitude, "Ciudad", "País");
        }
    }

    @Override
    public List<ZoneDetail> findNearbyZones(double latitude, double longitude, double radiusKm) {
        ZoneDetail center = resolveZoneFromCoordinates(latitude, longitude);
        return List.of(center);
    }
}