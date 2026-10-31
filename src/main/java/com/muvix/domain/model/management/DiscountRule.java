package com.muvix.domain.model.management;

import com.muvix.domain.model.exception.DomainException;

import java.math.BigDecimal;

/**
 * Precio dinámico: cuando faltan "diasRestantes" días o menos para la fecha límite,
 * se aplica "porcentaje" de descuento (0-100) sobre el price base.
 * Ej: (7, 10) -> última semana 10% off; (2, 30) -> últimos 2 días 30% off.
 */
public record DiscountRule(int daysLeft, BigDecimal percentage) {

    public DiscountRule {
        if (daysLeft < 0) throw new DomainException("Los días restantes no pueden ser negativos");
        if (percentage == null
                || percentage.compareTo(BigDecimal.ZERO) <= 0
                || percentage.compareTo(new BigDecimal("100")) >= 0) {
            throw new DomainException("El porcentaje debe estar entre 0 y 100 (exclusivo)");
        }
    }
}
