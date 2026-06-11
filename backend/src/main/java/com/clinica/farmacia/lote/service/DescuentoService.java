package com.clinica.farmacia.lote.service;

import com.clinica.farmacia.lote.entity.Lote;
import com.clinica.seguridad.service.ConfiguracionApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

/**
 * Auto-discount engine for near-expiry lots.
 *
 * DES-01: Discount applies if lote.fechaVencimiento <= today + umbral_dias.
 * DES-02: Discount = min(descuentoMax, descuentoFisico).
 *   - descuentoMax = precioOriginal × descuento_max_pct / 100
 *   - descuentoFisico = precioOriginal - costoMasIgv (floor: can't sell below cost+IGV)
 */
@Service
public class DescuentoService {

    private static final Logger log = LoggerFactory.getLogger(DescuentoService.class);

    static final int DEFAULT_DESCUENTO_VENCIMIENTO_DIAS = 90;
    static final BigDecimal DEFAULT_DESCUENTO_MAX_PCT = new BigDecimal("20");

    private static final BigDecimal CIEN = new BigDecimal("100");

    private final ConfiguracionApiService configService;

    public DescuentoService(ConfiguracionApiService configService) {
        this.configService = configService;
    }

    /**
     * Calculates the maximum applicable discount for a lot based on its expiry date.
     *
     * @param lote          the lot being sold
     * @param precioOriginal the original sale price
     * @param costoMasIgv   the cost + IGV floor
     * @return the discount amount to apply, or zero if no discount applies
     */
    public BigDecimal calcularDescuento(Lote lote, BigDecimal precioOriginal, BigDecimal costoMasIgv) {
        if (lote == null || precioOriginal == null || costoMasIgv == null) {
            return BigDecimal.ZERO;
        }

        int umbralDias = getConfigAsInteger("descuento_vencimiento_dias", DEFAULT_DESCUENTO_VENCIMIENTO_DIAS);
        LocalDate today = LocalDate.now();

        // DES-01: No discount if expiry is beyond threshold
        if (lote.getFechaVencimiento().isAfter(today.plusDays(umbralDias))) {
            log.debug("Lote {} vence {} (umbral={}d), sin descuento",
                lote.getCodigoLote(), lote.getFechaVencimiento(), umbralDias);
            return BigDecimal.ZERO;
        }

        BigDecimal descuentoMaxPct = getConfigAsDecimal("descuento_vencimiento_max_pct", DEFAULT_DESCUENTO_MAX_PCT);

        // DES-02: descuentoMax = precioOriginal × maxPct / 100
        BigDecimal descuentoMax = precioOriginal
            .multiply(descuentoMaxPct)
            .divide(CIEN, 4, RoundingMode.HALF_UP);

        // DES-02: descuentoFisico = precioOriginal - costoMasIgv
        BigDecimal descuentoFisico = precioOriginal.subtract(costoMasIgv);

        // If descuentoFisico is negative (price already below cost+IGV), floor to zero
        if (descuentoFisico.compareTo(BigDecimal.ZERO) < 0) {
            descuentoFisico = BigDecimal.ZERO;
        }

        // DES-02: Return the smaller of the two
        BigDecimal descuento = descuentoMax.min(descuentoFisico);

        log.debug("Descuento lote {}: precioOrig={}, costo+IGV={}, descMax={}, descFisico={}, descuento={}",
            lote.getCodigoLote(), precioOriginal, costoMasIgv, descuentoMax, descuentoFisico, descuento);

        return descuento;
    }

    private int getConfigAsInteger(String clave, int defaultValue) {
        try {
            var config = configService.findByModuloAndClave("farmacia", clave);
            return Integer.parseInt(config.valor());
        } catch (Exception e) {
            log.warn("Config 'farmacia/{}' not found, using default: {}", clave, defaultValue);
            return defaultValue;
        }
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
