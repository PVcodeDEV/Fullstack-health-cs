package com.clinica.clinica.admision.dto;

public record AdmisionDiagnosticoResponse(
    Long id,
    Long cuentaId,
    String codigoCie11,
    String descripcionCie11,
    String tipoDiagnostico,
    String descripcion
) {
    @Override
    public final String toString() {
        return "AdmisionDiagnosticoResponse{id=" + id + ", codigo=" + codigoCie11 + "}";
    }
}
