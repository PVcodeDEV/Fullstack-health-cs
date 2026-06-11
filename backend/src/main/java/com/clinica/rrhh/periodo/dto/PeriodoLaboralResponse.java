package com.clinica.rrhh.periodo.dto;

import com.clinica.rrhh.periodo.entity.PeriodoLaboral;

import java.time.LocalDate;

public record PeriodoLaboralResponse(
    Long id,
    Long trabajadorId,
    LocalDate fechaInicio,
    LocalDate fechaCese,
    String motivoCese,
    Boolean esReingreso,
    Boolean activo
) {
    public static PeriodoLaboralResponse fromEntity(PeriodoLaboral entity) {
        return new PeriodoLaboralResponse(
            entity.getId(),
            entity.getTrabajador().getId(),
            entity.getFechaInicio(),
            entity.getFechaCese(),
            entity.getMotivoCese(),
            entity.getEsReingreso(),
            entity.getActivo()
        );
    }
}
