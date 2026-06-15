package com.clinica.caja.liquidacion.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Unit tests for DescuentoValidator.
 * Covers LIQ-004 discount validation rules.
 */
class DescuentoValidatorTest {

    private DescuentoValidator validator;

    @BeforeEach
    void setUp() {
        validator = new DescuentoValidator();
    }

    // ============================================================
    // LIQ-004-1: Discount within limits (10% < 20%, no cost-floor issue)
    // ============================================================

    @Test
    void validar_WithDiscountWithinLimits_ShouldPass() {
        // GIVEN total=1000.00, discount=100.00 (10%), costo=500.00
        // WHEN/THEN final=900.00 > costo+IGV=590.00
        assertThatCode(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("100.00"),
            new BigDecimal("500.00"),
            1L))
            .doesNotThrowAnyException();
    }

    // ============================================================
    // LIQ-004-2: Discount exceeds 20% cap
    // ============================================================

    @Test
    void validar_WithDiscountExceeding20Percent_ShouldThrow() {
        // GIVEN total=1000.00, discount=250.00 (25%)
        // WHEN/THEN max allowed is 200.00 (20%)
        assertThatThrownBy(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("250.00"),
            new BigDecimal("500.00"),
            1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("20");
    }

    // ============================================================
    // LIQ-004-3: Discount below cost floor
    // ============================================================

    @Test
    void validar_WithDiscountBelowCostFloor_ShouldThrow() {
        // GIVEN total=700.00, discount=35.00 (5%), costo=600.00
        // costo+IGV = 600 * 1.18 = 708.00
        // final = 700 - 35 = 665.00 < 708.00
        assertThatThrownBy(() -> validator.validar(
            new BigDecimal("700.00"),
            new BigDecimal("35.00"),
            new BigDecimal("600.00"),
            1L))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("costo + IGV");
    }

    // ============================================================
    // LIQ-004-4: Discount without authorization
    // ============================================================

    @Test
    void validar_WithDiscountButNoApproval_ShouldThrow() {
        // GIVEN total=1000.00, discount=50.00, no usuarioApruebaId
        assertThatThrownBy(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("50.00"),
            new BigDecimal("500.00"),
            null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("autorización");
    }

    // ============================================================
    // Zero discount should pass without validation
    // ============================================================

    @Test
    void validar_WithZeroDiscount_ShouldPassWithoutApproval() {
        // GIVEN discount=0, no approval needed
        assertThatCode(() -> validator.validar(
            new BigDecimal("1000.00"),
            BigDecimal.ZERO,
            null,
            null))
            .doesNotThrowAnyException();
    }

    // ============================================================
    // Negative discount should be rejected
    // ============================================================

    @Test
    void validar_WithNegativeDiscount_ShouldThrow() {
        assertThatThrownBy(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("-50.00"),
            null,
            null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("negativo");
    }

    // ============================================================
    // Cost-floor check should be skipped when costo is null
    // ============================================================

    @Test
    void validar_WithNullCosto_ShouldSkipCostFloorCheck() {
        // GIVEN costo=null (not available in MVP)
        // THEN should validate percentage cap and approval, not cost-floor
        assertThatCode(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("100.00"),
            null,
            1L))
            .doesNotThrowAnyException();
    }

    // ============================================================
    // Boundary: exactly 20% discount should pass
    // ============================================================

    @Test
    void validar_WithExactly20PercentDiscount_ShouldPass() {
        assertThatCode(() -> validator.validar(
            new BigDecimal("1000.00"),
            new BigDecimal("200.00"),
            new BigDecimal("100.00"),
            1L))
            .doesNotThrowAnyException();
    }

    // ============================================================
    // Boundary: final price exactly equals costo+IGV should pass
    // ============================================================

    @Test
    void validar_WithFinalPriceEqualToCostoMasIgv_ShouldPass() {
        // GIVEN total=708.00, costo=600.00, costo+IGV=708.00
        // WHEN descuento=0 (final=708.00 == 708.00)
        assertThatCode(() -> validator.validar(
            new BigDecimal("708.00"),
            BigDecimal.ZERO,
            new BigDecimal("600.00"),
            null))
            .doesNotThrowAnyException();
    }
}
