package com.clinica.farmacia.lote.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record LoteRequest(

    @NotNull(message = "El producto es obligatorio")
    Long productoId,

    @NotBlank(message = "El código de lote es obligatorio")
    @Size(max = 100, message = "El código de lote no debe exceder 100 caracteres")
    String codigoLote,

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    LocalDate fechaVencimiento,

    @NotNull(message = "El stock inicial es obligatorio")
    @Min(value = 1, message = "El stock inicial debe ser mayor a cero")
    Integer stockInicial,

    @NotNull(message = "El precio de costo es obligatorio")
    @DecimalMin(value = "0.0001", message = "El precio de costo debe ser mayor a cero")
    @Digits(integer = 8, fraction = 4, message = "El precio de costo debe tener máximo 8 enteros y 4 decimales")
    BigDecimal precioCosto,

    @NotNull(message = "El almacén es obligatorio")
    Long almacenId,

    Long usuarioId,

    String motivo

) {}
