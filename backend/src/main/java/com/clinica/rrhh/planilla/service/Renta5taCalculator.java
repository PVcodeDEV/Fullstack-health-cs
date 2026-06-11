package com.clinica.rrhh.planilla.service;

import com.clinica.rrhh.planilla.config.PlanillaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class Renta5taCalculator {

    private static final Logger log = LoggerFactory.getLogger(Renta5taCalculator.class);
    private static final BigDecimal UIT_5 = new BigDecimal("5");
    private static final BigDecimal UIT_20 = new BigDecimal("20");
    private static final BigDecimal UIT_35 = new BigDecimal("35");
    private static final BigDecimal UIT_45 = new BigDecimal("45");
    private static final BigDecimal RATE_8 = new BigDecimal("0.08");
    private static final BigDecimal RATE_14 = new BigDecimal("0.14");
    private static final BigDecimal RATE_17 = new BigDecimal("0.17");
    private static final BigDecimal RATE_20 = new BigDecimal("0.20");
    private static final BigDecimal RATE_30 = new BigDecimal("0.30");
    private static final BigDecimal MONTHS_12 = new BigDecimal("12");

    private final PlanillaProperties properties;

    public Renta5taCalculator(PlanillaProperties properties) {
        this.properties = properties;
    }

    /**
     * Calcula la retención mensual de Renta 5ta Categoría.
     * @param remuneracionMensual sueldo bruto del mes
     * @param acumuladoAnual acumulado de meses anteriores en el año
     * @param mesesTrabajados meses trabajados en el año (incluyendo el actual)
     * @return monto a retener este mes
     */
    public BigDecimal calcular(BigDecimal remuneracionMensual,
                               BigDecimal acumuladoAnual,
                               int mesesTrabajados) {
        BigDecimal uit = BigDecimal.valueOf(properties.getUit());

        // Proyectar remuneración anual
        BigDecimal mesesRestantes = MONTHS_12.subtract(BigDecimal.valueOf(mesesTrabajados));
        BigDecimal proyeccionRestante = remuneracionMensual.multiply(mesesRestantes);
        BigDecimal proyectadoAnual = acumuladoAnual.add(proyeccionRestante);

        // Deducción: 7 UIT
        BigDecimal deduccion = uit.multiply(BigDecimal.valueOf(7));
        BigDecimal rentaNeta = proyectadoAnual.subtract(deduccion);

        if (rentaNeta.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }

        // Aplicar escala progresiva
        BigDecimal uitValor = uit;
        BigDecimal impuesto = BigDecimal.ZERO;
        BigDecimal remainder = rentaNeta;

        // Tramo 1: hasta 5 UIT → 8%
        BigDecimal tramo1 = uitValor.multiply(UIT_5);
        if (remainder.compareTo(tramo1) > 0) {
            impuesto = impuesto.add(tramo1.multiply(RATE_8));
            remainder = remainder.subtract(tramo1);
        } else {
            impuesto = impuesto.add(remainder.multiply(RATE_8));
            remainder = BigDecimal.ZERO;
        }

        // Tramo 2: 5-20 UIT → 14%
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tramo2 = uitValor.multiply(UIT_20.subtract(UIT_5));
            if (remainder.compareTo(tramo2) > 0) {
                impuesto = impuesto.add(tramo2.multiply(RATE_14));
                remainder = remainder.subtract(tramo2);
            } else {
                impuesto = impuesto.add(remainder.multiply(RATE_14));
                remainder = BigDecimal.ZERO;
            }
        }

        // Tramo 3: 20-35 UIT → 17%
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tramo3 = uitValor.multiply(UIT_35.subtract(UIT_20));
            if (remainder.compareTo(tramo3) > 0) {
                impuesto = impuesto.add(tramo3.multiply(RATE_17));
                remainder = remainder.subtract(tramo3);
            } else {
                impuesto = impuesto.add(remainder.multiply(RATE_17));
                remainder = BigDecimal.ZERO;
            }
        }

        // Tramo 4: 35-45 UIT → 20%
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal tramo4 = uitValor.multiply(UIT_45.subtract(UIT_35));
            if (remainder.compareTo(tramo4) > 0) {
                impuesto = impuesto.add(tramo4.multiply(RATE_20));
                remainder = remainder.subtract(tramo4);
            } else {
                impuesto = impuesto.add(remainder.multiply(RATE_20));
                remainder = BigDecimal.ZERO;
            }
        }

        // Tramo 5: exceso >45 UIT → 30%
        if (remainder.compareTo(BigDecimal.ZERO) > 0) {
            impuesto = impuesto.add(remainder.multiply(RATE_30));
        }

        // Mensual = impuesto anual / 12
        BigDecimal mensual = impuesto.divide(MONTHS_12, 2, RoundingMode.HALF_UP);
        log.debug("Renta 5ta: anual={}, neto={}, impuesto={}, mensual={}",
            proyectadoAnual, rentaNeta, impuesto, mensual);
        return mensual;
    }
}
