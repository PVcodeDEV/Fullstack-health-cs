package com.clinica.rrhh.planillaplame.dto;

import com.clinica.rrhh.planillaplame.entity.TRegistroEvento;
import java.time.LocalDate;

public record TRegistroEventoResponse(
    Long id,
    Long trabajadorId,
    Long contratoId,
    String tipoEvento,
    LocalDate fechaEvento,
    Long periodoPlanillaId,
    String estado
) {
    public static TRegistroEventoResponse fromEntity(TRegistroEvento entity) {
        return new TRegistroEventoResponse(
            entity.getId(),
            entity.getTrabajador().getId(),
            entity.getContrato() != null ? entity.getContrato().getId() : null,
            entity.getTipoEvento(),
            entity.getFechaEvento(),
            entity.getPeriodoPlanilla().getId(),
            entity.getEstado()
        );
    }
}
