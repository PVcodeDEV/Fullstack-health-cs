package com.clinica.rrhh.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SituacionEspecial {
    SIN_DISCAPACIDAD("Sin discapacidad", false, false),
    SOLO_DISCAPACIDAD("Con discapacidad", true, false),
    SOLO_SINDICALIZADO("Sindicalizado", false, true),
    AMBAS("Con discapacidad y sindicalizado", true, true);

    private final String descripcion;
    private final boolean discapacidad;
    private final boolean sindicalizado;
}
