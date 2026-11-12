
package com.muvix.infrastructure.adapters.in.rest.controller;

import com.muvix.application.port.out.location.LocationGatewayPort;
import com.muvix.domain.model.logistic.ZoneDetail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/locations")
public class LocationController {

    private final LocationGatewayPort locationGatewayPort;

    public LocationController(
            LocationGatewayPort locationGatewayPort
    ) {
        this.locationGatewayPort = locationGatewayPort;
    }

    @GetMapping("/resolve-zone")
    public ResponseEntity<ZoneDetail> resolveZone(
            @RequestParam double latitude,
            @RequestParam double longitude
    ) {

        ZoneDetail zone =
                locationGatewayPort.resolveZoneFromCoordinates(
                        latitude,
                        longitude
                );

        return ResponseEntity.ok(zone);
    }

    @GetMapping("/nearby-zones")
    public ResponseEntity<List<ZoneDetail>> getNearbyZones(
            @RequestParam double latitude,
            @RequestParam double longitude,
            @RequestParam(defaultValue = "5.0") double radiusKm
    ) {

        List<ZoneDetail> nearbyZones =
                locationGatewayPort.findNearbyZones(
                        latitude,
                        longitude,
                        radiusKm
                );

        return ResponseEntity.ok(nearbyZones);
    }
}
