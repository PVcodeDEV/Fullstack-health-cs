package com.clinica.caja.liquidacion.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Validates discount applications against business rules:
 * - Max 20% of total per LIQ-004-2
 * - Final price must not be less than costo + IGV per LIQ-004-3
 * - Discount > 0% requires Gerencia/Admin approval per LIQ-004-4
 */
@Component
public class DescuentoValidator {

    private static final Logger log = LoggerFactory.getLogger(DescuentoValidator.class);
    private static final BigDecimal MAX_DESCUENTO_PORCENTAJE = new BigDecimal("20.00");
    private static final BigDecimal IGV_PORCENTAJE = new BigDecimal("18.00");

    /**
     * Validates a discount application.
     *
     * @param total              total bill amount before discount
     * @param descuentoTotal     discount amount being applied
     * @param costoTotal         clinical cost of the bill (for cost-floor check)
     * @param usuarioApruebaId   user who approved the discount (null if no discount)
     * @throws IllegalArgumentException if any validation rule fails
     */
    public void validar(BigDecimal total, BigDecimal descuentoTotal, BigDecimal costoTotal,
                        Long usuarioApruebaId) {
        if (descuentoTotal == null || descuentoTotal.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El descuento no puede ser negativo");
        }

        if (descuentoTotal.compareTo(BigDecimal.ZERO) == 0) {
            return; // No discount, no validation needed
        }

        // LIQ-004-2: Discount > 20% of total is rejected
        BigDecimal maxDescuento = total.multiply(MAX_DESCUENTO_PORCENTAJE)
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        if (descuentoTotal.compareTo(maxDescuento) > 0) {
            log.warn("Discount {} exceeds max {} (20% of {})", descuentoTotal, maxDescuento, total);
            throw new IllegalArgumentException(
                "El descuento excede el máximo permitido de " + MAX_DESCUENTO_PORCENTAJE + "%");
        }

        // LIQ-004-4: Discount > 0% requires approval
        if (usuarioApruebaId == null) {
            throw new IllegalArgumentException(
                "Se requiere autorización (usuarioApruebaId) para aplicar descuentos");
        }

        // LIQ-004-3: Final price must not be below costo + IGV
        if (costoTotal != null && costoTotal.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal costoConIgv = costoTotal.multiply(BigDecimal.ONE.add(
                IGV_PORCENTAJE.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)));
            BigDecimal finalPrice = total.subtract(descuentoTotal);
            if (finalPrice.compareTo(costoConIgv) < 0) {
                log.warn("Final price {} is below costo+IGV {} (costo={})",
                    finalPrice, costoConIgv, costoTotal);
                throw new IllegalArgumentException(
                    "El precio final después del descuento no puede ser menor al costo + IGV");
            }
        }
    }

    /**
     * Returns the IGV percentage used by this validator.
     */
    public static BigDecimal getIgvPorcentaje() {
        return IGV_PORCENTAJE;
    }
}
