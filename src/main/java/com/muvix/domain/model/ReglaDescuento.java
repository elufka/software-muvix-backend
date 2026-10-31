package com.muvix.domain.model;

import java.math.BigDecimal;

/**
 * Precio dinámico: cuando faltan "diasRestantes" días o menos para la fecha límite,
 * se aplica "porcentaje" de descuento (0-100) sobre el precio base.
 * Ej: (7, 10) -> última semana 10% off; (2, 30) -> últimos 2 días 30% off.
 */
public record ReglaDescuento(int diasRestantes, BigDecimal porcentaje) {

    public ReglaDescuento {
        if (diasRestantes < 0) throw new DominioException("Los días restantes no pueden ser negativos");
        if (porcentaje == null
                || porcentaje.compareTo(BigDecimal.ZERO) <= 0
                || porcentaje.compareTo(new BigDecimal("100")) >= 0) {
            throw new DominioException("El porcentaje debe estar entre 0 y 100 (exclusivo)");
        }
    }
}
