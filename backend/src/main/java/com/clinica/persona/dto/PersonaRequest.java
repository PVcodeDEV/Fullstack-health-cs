package com.clinica.persona.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record PersonaRequest(
    @NotNull(message = "El tipo de documento es obligatorio")
    Long tipoDocumentoId,

    @NotBlank(message = "El número de documento es obligatorio")
    @Size(max = 20, message = "El número de documento no debe exceder 20 caracteres")
    String numeroDocumento,

    @Size(max = 200, message = "Los nombres no deben exceder 200 caracteres")
    String nombres,

    @Size(max = 100, message = "El apellido paterno no debe exceder 100 caracteres")
    String apellidoPaterno,

    @Size(max = 100, message = "El apellido materno no debe exceder 100 caracteres")
    String apellidoMaterno,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaNacimiento,

    @Pattern(regexp = "^[MF]$", message = "El sexo debe ser 'M' o 'F'")
    String sexo,

    Long estadoCivilId,

    @Size(max = 255, message = "La dirección no debe exceder 255 caracteres")
    String direccion,

    @Size(max = 6, message = "El ubigeo debe tener 6 caracteres")
    String ubigeoDistrito,

    @Size(max = 20, message = "El teléfono no debe exceder 20 caracteres")
    String telefono,

    @jakarta.validation.constraints.Email(message = "Debe proporcionar un email válido")
    @Size(max = 100, message = "El email no debe exceder 100 caracteres")
    String email
) {}
