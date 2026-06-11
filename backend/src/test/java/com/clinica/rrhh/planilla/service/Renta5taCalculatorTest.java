package com.clinica.rrhh.planilla.service;

import com.clinica.rrhh.planilla.config.PlanillaProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class Renta5taCalculatorTest {

    private Renta5taCalculator calculator;

    @BeforeEach
    void setUp() {
        var properties = new PlanillaProperties(1130, 5350, "20123456789");
        calculator = new Renta5taCalculator(properties);
    }

    @Test
    void below7Uit_ReturnsZero() {
        // remuneracion 1500/mes → proyectado 18000, deduccion 7*5350 = 37450
        // rentaNeta = 18000 - 37450 = -19450 → 0
        var result = calculator.calcular(
            new BigDecimal("1500"), BigDecimal.ZERO, 1);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void exactly7Uit_ReturnsZero() {
        // 7*5350 = 37450, 37450 / 12 ≈ 3120.83
        // Con 1 mes: proyectado = 3120.83 * 12 = 37450, rentaNeta = 0 → 0
        var result = calculator.calcular(
            new BigDecimal("3120.83"), BigDecimal.ZERO, 1);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    void firstBracket_8Percent() {
        // remuneracion 5000, 1 mes trabajado: proyectado = 5000 * 11 = 55000
        // deduccion 7*5350 = 37450, rentaNeta = 55000 - 37450 = 17550
        // tramo1: 5*5350 = 26750, 17550 < 26750 → 17550 * 0.08 = 1404
        // mensual = 1404 / 12 = 117.00
        var result = calculator.calcular(
            new BigDecimal("5000"), BigDecimal.ZERO, 1);
        assertThat(result).isEqualByComparingTo(new BigDecimal("117.00"));
    }

    @Test
    void secondBracket_14Percent() {
        // remuneracion 15000, 1 mes: proyectado = 15000 * 11 = 165000
        // deduccion 37450, rentaNeta = 127550
        // tramo1: 5*5350 = 26750 → 26750*0.08 = 2140, remainder = 100800
        // tramo2: 20-5 = 15 UIT = 80250, remainder 100800 > 80250 → 80250*0.14 = 11235, remainder = 20550
        // tramo3: 35-20 = 15 UIT = 80250, remainder 20550 < 80250 → 20550*0.17 = 3493.50
        // impuesto = 2140 + 11235 + 3493.50 = 16868.50
        // mensual = 16868.50/12 = 1405.71
        var result = calculator.calcular(
            new BigDecimal("15000"), BigDecimal.ZERO, 1);

        BigDecimal tramo1 = new BigDecimal("26750").multiply(new BigDecimal("0.08")); // 2140
        BigDecimal tramo2base = new BigDecimal("80250").multiply(new BigDecimal("0.14")); // 11235
        BigDecimal tramo3base = new BigDecimal("20550").multiply(new BigDecimal("0.17")); // 3493.50
        BigDecimal impuesto = tramo1.add(tramo2base).add(tramo3base);
        BigDecimal mensual = impuesto.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP); // 1405.71

        assertThat(result).isEqualByComparingTo(mensual);
    }

    @Test
    void highRemuneration_UsesAllBrackets() {
        // remuneracion 50000, 1 mes: proyectado = 50000 * 11 = 550000
        // deduccion 37450, rentaNeta = 512550
        // tramo1: 26750*0.08 = 2140, remainder = 485800
        // tramo2: 80250*0.14 = 11235, remainder = 405550
        // tramo3: 80250*0.17 = 13642.50, remainder = 325300
        // tramo4: 10*5350 = 53500 * 0.20 = 10700, remainder = 271800
        // tramo5: 271800*0.30 = 81540
        // impuesto = 2140 + 11235 + 13642.50 + 10700 + 81540 = 119257.50
        // mensual = 119257.50/12 = 9938.13
        var result = calculator.calcular(
            new BigDecimal("50000"), BigDecimal.ZERO, 1);

        BigDecimal fiveUit = new BigDecimal("26750");
        BigDecimal twentyUit = new BigDecimal("107000");
        BigDecimal thirtyfiveUit = new BigDecimal("187250");
        BigDecimal fortyfiveUit = new BigDecimal("240750");

        BigDecimal impuesto = fiveUit.multiply(new BigDecimal("0.08"))
            .add(twentyUit.subtract(fiveUit).multiply(new BigDecimal("0.14")))
            .add(thirtyfiveUit.subtract(twentyUit).multiply(new BigDecimal("0.17")))
            .add(fortyfiveUit.subtract(thirtyfiveUit).multiply(new BigDecimal("0.20")))
            .add(new BigDecimal("271800").multiply(new BigDecimal("0.30")));
        BigDecimal mensual = impuesto.divide(new BigDecimal("12"), 2, RoundingMode.HALF_UP); // 9938.13

        assertThat(result).isEqualByComparingTo(mensual);
    }

    @Test
    void withPriorAccumulation() {
        // remuneracion 5000, acumulado 30000 (6 meses previos), mesesTrabajados=7
        // proyectado = 30000 + 5000*5 = 55000
        // rentaNeta = 55000 - 37450 = 17550
        // tramo1: 17550*0.08 = 1404
        // mensual = 1404/12 = 117.00
        var result = calculator.calcular(
            new BigDecimal("5000"), new BigDecimal("30000"), 7);
        assertThat(result).isEqualByComparingTo(new BigDecimal("117.00"));
    }

    @Test
    void zeroRemuneracion_ReturnsZero() {
        var result = calculator.calcular(BigDecimal.ZERO, BigDecimal.ZERO, 1);
        assertThat(result).isEqualByComparingTo(BigDecimal.ZERO);
    }
}
