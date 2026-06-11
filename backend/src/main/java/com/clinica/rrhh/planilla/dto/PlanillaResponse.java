package com.clinica.rrhh.planilla.dto;

import com.clinica.rrhh.planilla.entity.Planilla;
import java.math.BigDecimal;
import java.time.LocalDate;

public record PlanillaResponse(
    Long id, Long periodoPlanillaId, String periodoLabel,
    LocalDate fechaLiquidacion,
    BigDecimal totalIngresos, BigDecimal totalDescuentos,
    BigDecimal totalAportes, BigDecimal totalNeto,
    Integer cantidadTrabajadores, String estado
) {
    @Override public final String toString() {
        return "PlanillaResponse{id=" + id + ", periodo=" + periodoLabel + ", estado=" + estado + "}";
    }
    public static PlanillaResponse fromEntity(Planilla e) {
        return new PlanillaResponse(
            e.getId(), e.getPeriodoPlanilla().getId(),
            e.getPeriodoPlanilla().getAnio() + "-" + String.format("%02d", e.getPeriodoPlanilla().getMes()),
            e.getFechaLiquidacion(),
            e.getTotalIngresos(), e.getTotalDescuentos(),
            e.getTotalAportes(), e.getTotalNeto(),
            e.getCantidadTrabajadores(), e.getEstado());
    }
}
