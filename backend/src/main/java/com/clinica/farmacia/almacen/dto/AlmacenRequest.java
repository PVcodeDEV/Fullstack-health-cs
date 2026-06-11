package com.clinica.farmacia.almacen.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AlmacenRequest(

    @NotBlank(message = "El código es obligatorio")
    @Size(max = 20, message = "El código no debe exceder 20 caracteres")
    String codigo,

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100, message = "El nombre no debe exceder 100 caracteres")
    String nombre,

    @Size(max = 255, message = "La ubicación no debe exceder 255 caracteres")
    String ubicacion,

    Boolean defaultWarehouse

) {}
