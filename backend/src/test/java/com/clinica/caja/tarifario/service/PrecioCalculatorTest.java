package com.clinica.caja.tarifario.service;

import com.clinica.caja.tarifario.service.PrecioCalculator.PricingProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PrecioCalculatorTest {

    private final PrecioCalculator calculator = new PrecioCalculator(
        new PricingProperties(18, 50));

    @Test
    void defaultConfig_ShouldCalculateCorrectly() {
        // precioBase=100.00, IGV=18%, utilidad=50%
        // IGV = 100 * 0.18 = 18.00
        // Utilidad = 100 * 0.50 = 50.00
        // Final = 100 + 18 + 50 = 168.00
        BigDecimal result = calculator.calcularPrecioFinal(new BigDecimal("100.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("168.00"));
    }

    @Test
    void customConfig_ShouldUseGivenPercentages() {
        // utilidad=60%, IGV=18%
        // IGV = 100 * 0.18 = 18.00
        // Utilidad = 100 * 0.60 = 60.00
        // Final = 100 + 18 + 60 = 178.00
        PrecioCalculator calc = new PrecioCalculator(new PricingProperties(18, 60));

        BigDecimal result = calc.calcularPrecioFinal(new BigDecimal("100.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("178.00"));
    }

    @Test
    void rounding_ShouldRoundToNearestTenCents() {
        // precioBase=85.00 → final=142.80 → already at 0.10 boundary
        BigDecimal result = calculator.calcularPrecioFinal(new BigDecimal("85.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("142.8"));

        // precioBase=85.03 → final~=142.8504 → *10=1428.504 → round=1429 → /10=142.9
        BigDecimal result2 = calculator.calcularPrecioFinal(new BigDecimal("85.03"));
        assertThat(result2).isEqualByComparingTo(new BigDecimal("142.9"));

        // precioBase=85.01 → final~=142.8168 → *10=1428.168 → round=1428 → /10=142.8
        BigDecimal result3 = calculator.calcularPrecioFinal(new BigDecimal("85.01"));
        assertThat(result3).isEqualByComparingTo(new BigDecimal("142.8"));
    }

    @Test
    void nullPrecioBase_ShouldThrow() {
        assertThatThrownBy(() -> calculator.calcularPrecioFinal(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("nulo");
    }

    @Test
    void negativePrecioBase_ShouldThrow() {
        assertThatThrownBy(() -> calculator.calcularPrecioFinal(new BigDecimal("-50.00")))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cero");
    }

    @Test
    void zeroPrecioBase_ShouldThrow() {
        assertThatThrownBy(() -> calculator.calcularPrecioFinal(BigDecimal.ZERO))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("cero");
    }

    @Test
    void smallPrecioBase_ShouldRoundCorrectly() {
        // precioBase=0.01, IGV~=0.0018, utilidad~=0.005
        // Final~=0.0168 → rounded to 0.00
        BigDecimal result = calculator.calcularPrecioFinal(new BigDecimal("0.01"));
        // 0.0168 * 10 = 0.168 → setScale(0) = 0 → /10 = 0.0
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO.setScale(1));
    }

    @Test
    void defaultConfig_IsPrecioBaseTimesOnePointSixEight() {
        // precioBase=250.00 → 250 * 1.68 = 420.00
        BigDecimal result = calculator.calcularPrecioFinal(new BigDecimal("250.00"));
        assertThat(result).isEqualByComparingTo(new BigDecimal("420.00"));
    }
}
