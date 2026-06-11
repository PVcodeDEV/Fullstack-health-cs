package com.clinica.farmacia.producto.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Utility for rounding prices to nearest 0.10 (half-up at 0.05).
 * Internal calc uses full BigDecimal precision; rounding only at API boundary.
 */
public final class RoundingUtil {

    private static final BigDecimal TENTH = new BigDecimal("0.10");
    private static final int PRECISION = 2;

    private RoundingUtil() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * Rounds a price to the nearest 0.10 with HALF_UP rounding.
     * Example: 7.05 → 7.10, 7.04 → 7.00, 7.15 → 7.20
     */
    public static BigDecimal roundPrecio(BigDecimal precio) {
        if (precio == null) return BigDecimal.ZERO.setScale(PRECISION);
        return precio
            .divide(TENTH, 0, RoundingMode.HALF_UP)
            .multiply(TENTH)
            .setScale(PRECISION, RoundingMode.HALF_UP);
    }

    /**
     * Spanish alias for {@link #roundPrecio}.
     */
    public static BigDecimal redondearPrecio(BigDecimal precio) {
        return roundPrecio(precio);
    }

    /**
     * Rounds a monetary amount to 2 decimal places with HALF_UP rounding.
     * Example: 10.345 → 10.35, 10.344 → 10.34
     */
    public static BigDecimal redondearMonto(BigDecimal monto) {
        if (monto == null) return BigDecimal.ZERO.setScale(PRECISION);
        return monto.setScale(PRECISION, RoundingMode.HALF_UP);
    }
}
