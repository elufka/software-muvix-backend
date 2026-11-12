package com.muvix.application.port.in.catalogue.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Read Model inmutable para la consulta de ítems del catálogo.
 */
public record CatalogItemReadModel(
        String itemId,
        String zoneId,
        String title,
        String description,
        BigDecimal price,
        boolean isBatch,
        String sellerId,
        String eventId,
        Instant createdAt
) {}
