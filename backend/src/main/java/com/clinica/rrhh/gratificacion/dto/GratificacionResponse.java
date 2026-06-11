package com.clinica.rrhh.gratificacion.dto;

import com.clinica.rrhh.gratificacion.entity.Gratificacion;
import java.math.BigDecimal;

public record GratificacionResponse(
    Long id, Long periodoPlanillaId, String periodoLabel,
    Long trabajadorId, String trabajadorNombre,
    Long contratoId, String semestre,
    Integer mesesComputables, BigDecimal remuneracionComputable,
    BigDecimal gratificacion, BigDecimal bonificacionExtraordinaria,
    BigDecimal total, String estado
) {
    @Override public final String toString() {
        return "GratificacionResponse{id=" + id + ", trabajadorId=" + trabajadorId + ", semestre=" + semestre + "}";
    }

    public static GratificacionResponse fromEntity(Gratificacion e) {
        return new GratificacionResponse(
            e.getId(), e.getPeriodoPlanilla().getId(),
            e.getPeriodoPlanilla().getAnio() + "-" + String.format("%02d", e.getPeriodoPlanilla().getMes()),
            e.getTrabajador().getId(),
            e.getTrabajador().getPersona().getNombres() + " " + e.getTrabajador().getPersona().getApellidoPaterno(),
            e.getContrato() != null ? e.getContrato().getId() : null,
            e.getSemestre(), e.getMesesComputables(), e.getRemuneracionComputable(),
            e.getGratificacion(), e.getBonificacionExtraordinaria(),
            e.getTotal(), e.getEstado());
    }
}
