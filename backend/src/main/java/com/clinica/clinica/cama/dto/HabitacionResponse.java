package com.clinica.clinica.cama.dto;

public record HabitacionResponse(
    Long id,
    Long tipoHabitacionId,
    String tipoHabitacionNombre,
    String nombre,
    String ubicacion,
    Integer capacidad,
    Boolean activo
) {
    @Override
    public final String toString() {
        return "HabitacionResponse{id=" + id + ", nombre=" + nombre + "}";
    }
}
