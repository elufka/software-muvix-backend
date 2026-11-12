package com.muvix.application.port.out.location;

import com.muvix.domain.model.logistic.ZoneDetail;
import java.util.List;

public interface LocationGatewayPort {
    ZoneDetail resolveZoneFromCoordinates(double latitude, double longitude);
    List<ZoneDetail> findNearbyZones(double latitude, double longitude, double radiusKm);
}