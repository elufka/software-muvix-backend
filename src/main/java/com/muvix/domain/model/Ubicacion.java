package com.muvix.domain.model;

/**
 * Value object. La zona es pública (visible en el catálogo); la dirección exacta
 * solo se revela al comprador cuando confirma su turno de retiro.
 */
public record Ubicacion(String zona, String direccionExacta) {

    public Ubicacion {
        if (zona == null || zona.isBlank()) throw new DominioException("La zona es obligatoria");
    }

    /** Versión segura para exponer en el catálogo público. */
    public Ubicacion publica() {
        return new Ubicacion(zona, null);
    }
}
