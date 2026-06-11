package com.clinica.rrhh.pension.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record InformacionPensionariaRequest(

    @NotNull(message = "La AFP es obligatoria")
    Long afpId,

    @Size(max = 16, message = "CUSPP debe tener máximo 16 caracteres")
    String cuspp,

    String comisionTipo,

    Boolean sctr,

    @NotNull(message = "La fecha de afiliación es obligatoria")
    LocalDate fechaAfiliacion,

    @Size(max = 50, message = "Documento de referencia debe tener máximo 50 caracteres")
    String documentoReferencia

) {}
