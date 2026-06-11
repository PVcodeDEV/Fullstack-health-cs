package com.clinica.farmacia.venta.dto;

import com.clinica.farmacia.venta.type.TipoLista;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VentaRequest(

    @NotNull(message = "La sesión de caja es obligatoria")
    Long sesionCajaId,

    Long clientePersonaId,

    @NotNull(message = "El tipo de lista es obligatorio")
    TipoLista tipoLista,

    @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
    String observaciones,

    @NotNull(message = "Los detalles de venta son obligatorios")
    @NotEmpty(message = "Debe incluir al menos un detalle")
    @Valid
    List<DetalleVentaRequest> detalles

) {
    public VentaRequest {
        if (tipoLista == null) {
            tipoLista = TipoLista.PUBLICO;
        }
    }
}
