package com.clinica.caja.tarifario.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing formula service — computes precioFinal from precioBase.
 * <p>
 * Percentages (IGV and utilidad clínica) are read from application configuration,
 * defaulting to 18% IGV and 50% utilidad. Per TRF-003, these should eventually come
 * from tb_configuracion_api — the value from config is the fallback.
 * <p>
 * Formula: precioFinal = precioBase + IGV + utilidad
 * Rounded to nearest 0.10 (half-up at 0.05).
 * Equivalent to: precioFinal = precioBase × 1.68 (with default percentages)
 */
@Service
public class PrecioCalculator {

    private static final Logger log = LoggerFactory.getLogger(PrecioCalculator.class);

    private final PricingProperties properties;

    public PrecioCalculator(PricingProperties properties) {
        this.properties = properties;
    }

    /**
     * Compute final price from base price using configured percentages.
     *
     * @param precioBase the base cost before markup
     * @return the computed final price, rounded to nearest 0.10
     */
    public BigDecimal calcularPrecioFinal(BigDecimal precioBase) {
        if (precioBase == null) {
            throw new IllegalArgumentException("precioBase no puede ser nulo");
        }
        if (precioBase.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("precioBase debe ser mayor a cero");
        }

        BigDecimal igvRate = BigDecimal.valueOf(properties.igvPorcentaje())
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal utilidadRate = BigDecimal.valueOf(properties.utilidadPorcentaje())
            .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);

        BigDecimal igv = precioBase.multiply(igvRate);
        BigDecimal utilidad = precioBase.multiply(utilidadRate);

        BigDecimal finalPrice = precioBase.add(igv).add(utilidad);

        // Round to nearest 0.10 (half-up at 0.05)
        BigDecimal result = finalPrice.multiply(BigDecimal.TEN)
            .setScale(0, RoundingMode.HALF_UP)
            .divide(BigDecimal.TEN, 1, RoundingMode.HALF_UP);

        log.debug("Precio calculated: base={}, igv={}%, utilidad={}%, final={}",
            precioBase, properties.igvPorcentaje(), properties.utilidadPorcentaje(), result);
        return result;
    }

    /**
     * Configuration properties for pricing percentages.
     * Maps from {@code app.caja.pricing.*} in application.yml.
     * <p>
     * Future: these defaults will be overridden by tb_configuracion_api values.
     */
    @ConfigurationProperties(prefix = "app.caja.pricing")
    public record PricingProperties(
        @DefaultValue("18") int igvPorcentaje,
        @DefaultValue("50") int utilidadPorcentaje
    ) {}
}
