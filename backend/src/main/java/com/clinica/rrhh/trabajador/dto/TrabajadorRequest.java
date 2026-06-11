package com.clinica.rrhh.trabajador.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

public record TrabajadorRequest(

    @NotNull(message = "La persona es obligatoria")
    Long personaId,

    @NotBlank(message = "El código de trabajador es obligatorio")
    @Size(max = 20, message = "El código de trabajador no debe exceder 20 caracteres")
    String codigoTrabajador,

    @NotNull(message = "La fecha de ingreso es obligatoria")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate fechaIngreso,

    String tipo,                    // TipoTrabajador enum name

    String regimenLaboral,          // RegimenLaboral enum name

    @Size(max = 200, message = "El cargo no debe exceder 200 caracteres")
    String cargo,

    Long areaFuncionalId,

    @Size(max = 60, message = "El banco no debe exceder 60 caracteres")
    String banco,

    @Size(max = 20, message = "La cuenta de sueldo no debe exceder 20 caracteres")
    String cuentaSueldo,

    @Size(max = 23, message = "El CCI no debe exceder 23 caracteres")
    String cci,

    @Size(max = 150, message = "El nombre de contacto no debe exceder 150 caracteres")
    String contactoNombre,

    @Size(max = 15, message = "El teléfono de contacto no debe exceder 15 caracteres")
    String contactoTelefono,

    Integer cantidadHijos,

    @Size(max = 20, message = "El número de colegiatura no debe exceder 20 caracteres")
    String nroColegiatura,

    Long tipoColegiaturaId,         // FK to TipoColegiatura

    Boolean discapacidad,

    Boolean sindicalizado

) {}
