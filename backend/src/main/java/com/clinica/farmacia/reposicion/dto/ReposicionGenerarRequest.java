package com.clinica.farmacia.reposicion.dto;

import jakarta.validation.constraints.Size;

public record ReposicionGenerarRequest(

    Long almacenId,

    Boolean critico,

    @Size(max = 500, message = "Las observaciones no deben exceder 500 caracteres")
    String observaciones

) {
    public ReposicionGenerarRequest {
        if (critico == null) {
            critico = false;
        }
        if (observaciones == null) {
            observaciones = "";
        }
    }
}
