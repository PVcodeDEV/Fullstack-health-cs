package com.clinica.clinica.medico.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MedicoRequest(

    @NotNull(message = "La persona es obligatoria")
    Long personaId,

    Long trabajadorId,

    @NotBlank(message = "El CMP es obligatorio")
    @Size(max = 20, message = "El CMP no debe exceder 20 caracteres")
    String cmp,

    Long especialidadId,

    Boolean esEspecialista

) {}
