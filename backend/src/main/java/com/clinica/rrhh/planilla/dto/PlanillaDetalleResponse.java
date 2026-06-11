package com.clinica.rrhh.planilla.dto;

import com.clinica.rrhh.planilla.entity.PlanillaDetalle;
import java.math.BigDecimal;

public record PlanillaDetalleResponse(
    Long id, Long planillaId, Long trabajadorId, String trabajadorNombre,
    Long contratoId,
    BigDecimal sueldoBase, BigDecimal asignacionFamiliar,
    Integer diasLaborados,
    BigDecimal totalIngresos, BigDecimal totalDescuentos,
    BigDecimal totalAportes, BigDecimal neto,
    String conceptosJson
) {
    @Override public final String toString() {
        return "PlanillaDetalleResponse{id=" + id + ", trabajadorId=" + trabajadorId + "}";
    }
    public static PlanillaDetalleResponse fromEntity(PlanillaDetalle e) {
        return new PlanillaDetalleResponse(
            e.getId(), e.getPlanilla().getId(),
            e.getTrabajador().getId(),
            e.getTrabajador().getPersona().getNombres() + " " + e.getTrabajador().getPersona().getApellidoPaterno(),
            e.getContrato() != null ? e.getContrato().getId() : null,
            e.getSueldoBase(), e.getAsignacionFamiliar(),
            e.getDiasLaborados(),
            e.getTotalIngresos(), e.getTotalDescuentos(),
            e.getTotalAportes(), e.getNeto(),
            e.getConceptosJson());
    }
}
