package com.clinica.clinica.admision.dto;

import jakarta.validation.constraints.NotNull;

public record AsignarCamaRequest(
    @NotNull Long camaId,
    Long solicitudHospitalizacionId
) {}
