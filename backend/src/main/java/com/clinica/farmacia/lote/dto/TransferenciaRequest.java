package com.clinica.farmacia.lote.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferenciaRequest(

    @NotNull(message = "El producto es obligatorio")
    Long productoId,

    @NotNull(message = "El lote de origen es obligatorio")
    Long loteOrigenId,

    @NotNull(message = "El almacén de destino es obligatorio")
    Long almacenDestinoId,

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor a cero")
    Integer cantidad,

    @NotBlank(message = "El motivo es obligatorio")
    String motivo

) {}
