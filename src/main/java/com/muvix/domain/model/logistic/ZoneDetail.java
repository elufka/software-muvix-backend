package com.muvix.domain.model.logistic;

/**
 * Value Object inmutable que representa la información normalizada de una zona geográfica.
 */
public record ZoneDetail(
        String zoneId,
        String name,
        double latitude,
        double longitude,
        String city,
        String country
) {
    public ZoneDetail {
        if (zoneId == null || zoneId.isBlank()) {
            throw new IllegalArgumentException("El zoneId no puede ser nulo ni estar vacío.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("El nombre de la zona es obligatorio.");
        }
    }
}