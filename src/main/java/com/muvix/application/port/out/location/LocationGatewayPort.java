package com.muvix.application.port.out.location;

import java.util.List;

public interface LocationGatewayPort {

    record GeoCoordinates(double latitude, double longitude) {}

    /** Resuelve el código/nombre de zona normalizada a partir de coordenadas GPS */
    String resolveZoneFromCoordinates(GeoCoordinates coordinates);

    /** Retorna la lista de códigos de zonas adyacentes/cercanas a una zona dada */
    List<String> findNearbyZones(String baseZone, double radiusKm);
}