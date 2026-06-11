package com.clinica.maestro.dto.clinico;

import com.clinica.maestro.entity.clinico.TipoHabitacion;

import java.math.BigDecimal;

public record TipoHabitacionResponse(
    Long id,
    String codigo,
    String nombre,
    BigDecimal tarifaBase,
    Integer capacidad,
    Boolean activo
) {
    public static TipoHabitacionResponse fromEntity(TipoHabitacion entity) {
        return new TipoHabitacionResponse(
            entity.getId(),
            entity.getCodigo(),
            entity.getNombre(),
            entity.getTarifaBase(),
            entity.getCapacidad(),
            entity.getActivo()
        );
    }
}
