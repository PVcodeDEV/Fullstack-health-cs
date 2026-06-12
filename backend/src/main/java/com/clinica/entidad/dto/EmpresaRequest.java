package com.clinica.entidad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record EmpresaRequest(

    @NotBlank(message = "El RUC es obligatorio")
    @Pattern(regexp = "\\d{11}", message = "El RUC debe tener 11 dígitos")
    String ruc,

    @Size(max = 255, message = "La razón social no debe exceder 255 caracteres")
    String razonSocial,

    @Size(max = 255, message = "La dirección fiscal no debe exceder 255 caracteres")
    String direccionFiscal,

    @Size(max = 6, message = "El ubigeo debe tener máximo 6 caracteres")
    String ubigeo,

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    String telefono,

    @Size(max = 100, message = "El email no debe exceder 100 caracteres")
    String email,

    Long personaId

) {}
