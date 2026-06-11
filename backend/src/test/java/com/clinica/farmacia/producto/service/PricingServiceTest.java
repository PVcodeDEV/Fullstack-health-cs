package com.clinica.farmacia.producto.service;

import com.clinica.seguridad.dto.ConfiguracionApiResponse;
import com.clinica.seguridad.service.ConfiguracionApiService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PricingServiceTest {

    @Mock
    private ConfiguracionApiService configService;

    private PricingService pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(configService);
    }

    @Test
    void shouldCalculatePriceWithDefaultUtilidad() {
        // costo=5.00, utilidad_base=20% → IGV=0.90, costo+IGV=5.90, precio=7.08 → round to 7.10
        // SC-04: costo=5.00, utilidad=20% → precio=7.10
        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("5.00"), new BigDecimal("20"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("7.10"));
    }

    @Test
    void shouldUseDefaultUtilidadForLowCost() {
        // costo <= umbral (90) → utilidad_base (20%)
        BigDecimal utilidad = pricingService.calcularUtilidadDefault(new BigDecimal("5.00"));
        assertThat(utilidad).isEqualByComparingTo(new BigDecimal("20"));
    }

    @Test
    void shouldUseMidpointForHighCost() {
        // costo > umbral (90) → utilidad in [10..20], default midpoint = 15
        // SC-05: costo=120 (>90) → utilidad=15%
        BigDecimal utilidad = pricingService.calcularUtilidadDefault(new BigDecimal("120"));
        assertThat(utilidad).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void shouldRoundToNearestTenth() {
        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("10.00"), new BigDecimal("10"));
        // costo=10, IGV=1.80, costo+IGV=11.80, utilidad=10%, precioRaw=12.98, round to 13.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("13.00"));
    }

    @Test
    void shouldRejectNegativeUtilidad() {
        // SC-06: utilidad=-5% on costo=5.00 → rejected
        assertThatThrownBy(() -> pricingService.calcularPrecioVenta(new BigDecimal("5.00"), new BigDecimal("-5")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La utilidad no puede ser nula o negativa");
    }

    @Test
    void shouldRejectNegativeCost() {
        assertThatThrownBy(() -> pricingService.calcularPrecioVenta(new BigDecimal("-10"), new BigDecimal("20")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El costo no puede ser nulo o negativo");
    }

    @Test
    void shouldRejectNullCost() {
        assertThatThrownBy(() -> pricingService.calcularPrecioVenta(null, new BigDecimal("20")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("El costo no puede ser nulo o negativo");
    }

    @Test
    void shouldRejectNullUtilidad() {
        assertThatThrownBy(() -> pricingService.calcularPrecioVenta(new BigDecimal("5.00"), null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("La utilidad no puede ser nula o negativa");
    }

    @Test
    void shouldValidatePrecioMinimoPass() {
        boolean valid = pricingService.validarPrecioMinimo(new BigDecimal("10.00"), new BigDecimal("5.00"));
        assertThat(valid).isTrue();
    }

    @Test
    void shouldValidatePrecioMinimoFail() {
        boolean valid = pricingService.validarPrecioMinimo(new BigDecimal("5.00"), new BigDecimal("10.00"));
        assertThat(valid).isFalse();
    }

    @Test
    void shouldHandleZeroCost() {
        BigDecimal result = pricingService.calcularPrecioVenta(BigDecimal.ZERO, new BigDecimal("20"));
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
    }

    @Test
    void shouldHandleLargeCost() {
        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("1000.00"), new BigDecimal("15"));
        // costo=1000, IGV=180, costo+IGV=1180, utilidad=15%, precioRaw=1357, round to 1357.00
        assertThat(result).isEqualByComparingTo(new BigDecimal("1357.00"));
    }

    @Test
    void shouldUseCustomConfigWhenAvailable() {
        when(configService.findByModuloAndClave("farmacia", "igv"))
            .thenReturn(new ConfiguracionApiResponse(1L, "farmacia", "igv", "10", "decimal", true));
        System.out.println(">>> Mock igv set up");

        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("100.00"), new BigDecimal("25"));
        // IGV=10% → 10, costo+IGV=110, utilidad=25%, precioRaw=137.5, round to 137.50
        assertThat(result).isEqualByComparingTo(new BigDecimal("137.50"));
    }

    @Test
    void shouldCalculateUtilidadDefaultWithCustomUmbral() {
        when(configService.findByModuloAndClave("farmacia", "umbral_costo"))
            .thenReturn(new ConfiguracionApiResponse(3L, "farmacia", "umbral_costo", "50", "decimal", true));

        BigDecimal utilidad = pricingService.calcularUtilidadDefault(new BigDecimal("75"));
        // 75 > 50 → utilidad_alta (midpoint of 10-20 = 15)
        assertThat(utilidad).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void shouldHandleExactUmbralBoundary() {
        BigDecimal utilidadBelow = pricingService.calcularUtilidadDefault(new BigDecimal("90"));
        assertThat(utilidadBelow).isEqualByComparingTo(new BigDecimal("20"));

        BigDecimal utilidadAbove = pricingService.calcularUtilidadDefault(new BigDecimal("90.0001"));
        assertThat(utilidadAbove).isEqualByComparingTo(new BigDecimal("15"));
    }

    @Test
    void shouldHandleSmallFractionalPrices() {
        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("1.50"), new BigDecimal("20"));
        // IGV=0.27, costo+IGV=1.77, utilidad=20%, precioRaw=2.124, round to 2.10
        assertThat(result).isEqualByComparingTo(new BigDecimal("2.10"));
    }

    @Test
    void shouldReturnScaleTwoResult() {
        BigDecimal result = pricingService.calcularPrecioVenta(new BigDecimal("5.00"), new BigDecimal("20"));
        assertThat(result.scale()).isEqualTo(2);
    }
}
