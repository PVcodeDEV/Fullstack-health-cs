package com.clinica.caja.tarifario.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public record PaqueteRequest(

    @NotBlank(message = "El código es obligatorio")
    @jakarta.validation.constraints.Size(max = 50, message = "El código no debe exceder 50 caracteres")
    String codigo,

    @NotBlank(message = "El nombre es obligatorio")
    @jakarta.validation.constraints.Size(max = 200, message = "El nombre no debe exceder 200 caracteres")
    String nombre,

    @jakarta.validation.constraints.Size(max = 500, message = "La descripción no debe exceder 500 caracteres")
    String descripcion,

    @NotNull(message = "El precio total es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio total debe ser mayor a cero")
    @Digits(integer = 8, fraction = 2, message = "El precio total debe tener máximo 8 enteros y 2 decimales")
    BigDecimal precioTotal,

    @Valid
    List<PaqueteItemRequest> items

) {

    public record PaqueteItemRequest(
        @NotNull(message = "El id del tarifario item es obligatorio")
        Long tarifarioItemId,

        @NotNull(message = "La cantidad es obligatoria")
        @DecimalMin(value = "1", message = "La cantidad debe ser al menos 1")
        Integer cantidad
    ) {}
}
