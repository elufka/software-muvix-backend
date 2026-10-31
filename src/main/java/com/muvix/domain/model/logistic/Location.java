package com.muvix.domain.model.logistic;

import com.muvix.domain.model.exception.DomainException;

/**
 * Value object. La zona es pública (visible en el catálogo); la dirección exacta
 * solo se revela al comprador cuando confirma su turno de retiro.
 */
public record Location(String zona, String direccionExacta) {

    public Location {
        if (zona == null || zona.isBlank()) throw new DomainException("La zona es obligatoria");
    }

    /** Versión segura para exponer en el catálogo público. */
    public Location publica() {
        return new Location(zona, null);
    }
}
