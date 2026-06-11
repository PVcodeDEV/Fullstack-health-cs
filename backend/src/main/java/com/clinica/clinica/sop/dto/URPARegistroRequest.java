package com.clinica.clinica.sop.dto;

import jakarta.validation.constraints.NotNull;

public record URPARegistroRequest(
    @NotNull Integer escalaAldreteIngreso,
    String condicionIngreso,
    String observaciones
) {}
