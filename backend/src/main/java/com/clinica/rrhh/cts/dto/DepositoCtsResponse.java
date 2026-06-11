package com.clinica.rrhh.cts.dto;

import com.clinica.rrhh.cts.entity.DepositoCts;
import java.math.BigDecimal;

public record DepositoCtsResponse(
    Long id, Long periodoPlanillaId, String periodoLabel,
    Long trabajadorId, String trabajadorNombre,
    Long contratoId, String semestre,
    Integer diasComputables, BigDecimal remuneracionComputable,
    BigDecimal promedioGratificacion, BigDecimal promedioBonificacion,
    BigDecimal montoCts, String estado
) {
    @Override public final String toString() {
        return "DepositoCtsResponse{id=" + id + ", trabajadorId=" + trabajadorId + ", semestre=" + semestre + "}";
    }

    public static DepositoCtsResponse fromEntity(DepositoCts e) {
        return new DepositoCtsResponse(
            e.getId(), e.getPeriodoPlanilla().getId(),
            e.getPeriodoPlanilla().getAnio() + "-" + String.format("%02d", e.getPeriodoPlanilla().getMes()),
            e.getTrabajador().getId(),
            e.getTrabajador().getPersona().getNombres() + " " + e.getTrabajador().getPersona().getApellidoPaterno(),
            e.getContrato() != null ? e.getContrato().getId() : null,
            e.getSemestre(), e.getDiasComputables(), e.getRemuneracionComputable(),
            e.getPromedioGratificacion(), e.getPromedioBonificacion(),
            e.getMontoCts(), e.getEstado());
    }
}
