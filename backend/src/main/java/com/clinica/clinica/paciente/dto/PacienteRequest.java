package com.clinica.clinica.paciente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PacienteRequest(

    @NotNull(message = "La persona es obligatoria")
    Long personaId,

    @NotBlank(message = "El tipo de paciente es obligatorio")
    @Size(max = 20, message = "El tipo de paciente no debe exceder 20 caracteres")
    String tipoPaciente,

    @Size(max = 20, message = "El número de historia clínica no debe exceder 20 caracteres")
    String nroHistoriaClinica,

    @Pattern(regexp = "^(A|B|AB|O)[+-]$", message = "El grupo sanguíneo debe ser A+, A-, B+, B-, AB+, AB-, O+ u O-")
    @Size(max = 5)
    String grupoSanguineo,

    String alergias,

    @Size(max = 200, message = "El nombre de contacto de emergencia no debe exceder 200 caracteres")
    String contactoEmergenciaNombre,

    @Size(max = 20, message = "El teléfono de contacto de emergencia no debe exceder 20 caracteres")
    String contactoEmergenciaTelefono

) {}
