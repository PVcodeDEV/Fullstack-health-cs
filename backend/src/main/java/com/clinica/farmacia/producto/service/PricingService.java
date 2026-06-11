package com.clinica.farmacia.producto.service;

import com.clinica.farmacia.producto.util.RoundingUtil;
import com.clinica.seguridad.service.ConfiguracionApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Pricing engine for pharmacy products.
 *
 * Formula: precioVenta = (costo + IGV) × (1 + utilidad%)
 * Where IGV = 18% of costo.
 *
 * Threshold: costo <= umbral → utilidad_base (20%)
 *            costo > umbral  → utilidad in [utilidad_alta_min..utilidad_alta_max]
 *
 * Config keys (module "farmacia"): umbral_costo, utilidad_base, utilidad_alta_min, utilidad_alta_max, igv
 */
@Service
public class PricingService {

    private static final Logger log = LoggerFactory.getLogger(PricingService.class);

    static final BigDecimal DEFAULT_IGV = new BigDecimal("18");
    static final BigDecimal DEFAULT_UMBRAL = new BigDecimal("90");
    static final BigDecimal DEFAULT_UTILIDAD_BASE = new BigDecimal("20");
    static final BigDecimal DEFAULT_UTILIDAD_ALTA_MIN = new BigDecimal("10");
    static final BigDecimal DEFAULT_UTILIDAD_ALTA_MAX = new BigDecimal("20");

    private static final BigDecimal CERO = BigDecimal.ZERO;
    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ConfiguracionApiService configService;

    public PricingService(ConfiguracionApiService configService) {
        this.configService = configService;
    }

    /**
     * Calculates the sale price for a given cost and utility percentage.
     * Internal calc uses full BigDecimal precision; rounding at boundary only.
     *
     * @param costo     the product cost (precioCosto)
     * @param utilidadPct the utility percentage (e.g. 20 for 20%)
     * @return the calculated rounded sale price
     */
    public BigDecimal calcularPrecioVenta(BigDecimal costo, BigDecimal utilidadPct) {
        if (costo == null || costo.compareTo(CERO) < 0) {
            throw new IllegalArgumentException("El costo no puede ser nulo o negativo");
        }
        if (utilidadPct == null || utilidadPct.compareTo(CERO) < 0) {
            throw new IllegalArgumentException("La utilidad no puede ser nula o negativa");
        }

        BigDecimal igvRate = getConfigAsDecimal("igv", DEFAULT_IGV);
        BigDecimal igv = costo.multiply(igvRate).divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal costoMasIgv = costo.add(igv);

        BigDecimal utilidadFactor = utilidadPct.divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal precio = costoMasIgv.multiply(BigDecimal.ONE.add(utilidadFactor));

        BigDecimal rounded = RoundingUtil.roundPrecio(precio);

        log.debug("Pricing: costo={}, utilidad={}%, igv={}, costo+igv={}, precioRaw={}, rounded={}",
            costo, utilidadPct, igv, costoMasIgv, precio, rounded);

        return rounded;
    }

    /**
     * Calculates the default utility percentage based on cost threshold.
     */
    public BigDecimal calcularUtilidadDefault(BigDecimal costo) {
        if (costo == null) return DEFAULT_UTILIDAD_BASE;

        BigDecimal umbral = getConfigAsDecimal("umbral_costo", DEFAULT_UMBRAL);

        if (costo.compareTo(umbral) <= 0) {
            BigDecimal base = getConfigAsDecimal("utilidad_base", DEFAULT_UTILIDAD_BASE);
            log.debug("Costo {} <= umbral {}, utilidad_base={}", costo, umbral, base);
            return base;
        }

        BigDecimal min = getConfigAsDecimal("utilidad_alta_min", DEFAULT_UTILIDAD_ALTA_MIN);
        BigDecimal max = getConfigAsDecimal("utilidad_alta_max", DEFAULT_UTILIDAD_ALTA_MAX);
        BigDecimal utilidad = max.add(min).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP);

        log.debug("Costo {} > umbral {}, utilidad={} (min={}, max={})", costo, umbral, utilidad, min, max);
        return utilidad;
    }

    /**
     * Validates that precioVenta >= costo + IGV.
     */
    public boolean validarPrecioMinimo(BigDecimal precioVenta, BigDecimal costo) {
        if (precioVenta == null || costo == null) return false;

        BigDecimal igvRate = getConfigAsDecimal("igv", DEFAULT_IGV);
        BigDecimal igv = costo.multiply(igvRate).divide(CIEN, 4, RoundingMode.HALF_UP);
        BigDecimal costoMasIgv = costo.add(igv);

        return precioVenta.compareTo(costoMasIgv) >= 0;
    }

    private BigDecimal getConfigAsDecimal(String clave, BigDecimal defaultValue) {
        try {
            var config = configService.findByModuloAndClave("farmacia", clave);
            return new BigDecimal(config.valor());
        } catch (Exception e) {
            log.warn("Config 'farmacia/{}' not found, using default: {}", clave, defaultValue);
            return defaultValue;
        }
    }
}
