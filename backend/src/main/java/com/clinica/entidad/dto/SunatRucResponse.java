package com.clinica.entidad.dto;

/**
 * Response from SUNAT RUC API consultation.
 * RUC 20: razonSocial + direccionFiscal + ubigeo
 * RUC 10: nombreCompleto (from apenomdenunciado)
 */
public record SunatRucResponse(
    String ruc,
    String razonSocial,
    String nombreCompleto,
    String direccionFiscal,
    String ubigeo,
    Boolean exito
) {}
