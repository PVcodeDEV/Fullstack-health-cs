package com.clinica.farmacia.producto.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class RoundingUtilTest {

    @ParameterizedTest
    @CsvSource({
        "7.05,   7.10",
        "7.04,   7.00",
        "7.15,   7.20",
        "7.10,   7.10",
        "7.00,   7.00",
        "0.00,   0.00",
        "0.05,   0.10",
        "0.04,   0.00",
        "9.99,   10.00",
        "10.01,  10.00",
        "14.95,  15.00",
        "14.94,  14.90",
        "0.10,   0.10",
        "1.50,   1.50",
        "1.55,   1.60",
        "25.45,  25.50",
        "25.44,  25.40",
        "100.00, 100.00",
        "999.99, 1000.00",
    })
    void shouldRoundToNearestTenth(BigDecimal input, BigDecimal expected) {
        assertThat(RoundingUtil.roundPrecio(input)).isEqualByComparingTo(expected);
    }

    @Test
    void shouldHandleNullInput() {
        assertThat(RoundingUtil.roundPrecio(null)).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
    }

    @Test
    void shouldHandleZero() {
        assertThat(RoundingUtil.roundPrecio(BigDecimal.ZERO)).isEqualByComparingTo(BigDecimal.ZERO.setScale(2));
    }

    @Test
    void shouldResultHaveScaleTwo() {
        assertThat(RoundingUtil.roundPrecio(new BigDecimal("7.05")).scale()).isEqualTo(2);
        assertThat(RoundingUtil.roundPrecio(new BigDecimal("0.00")).scale()).isEqualTo(2);
    }
}
