package com.clinica.seguridad.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ConfiguracionApiRequest(
    @NotBlank(message = "El módulo es obligatorio")
    @Size(max = 50, message = "El módulo no debe exceder 50 caracteres")
    String modulo,

    @NotBlank(message = "La clave es obligatoria")
    @Size(max = 100, message = "La clave no debe exceder 100 caracteres")
    String clave,

    String valor,

    @Size(max = 20, message = "El tipo no debe exceder 20 caracteres")
    String tipo
) {}
