package com.clinica.rrhh.planilla.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

@Validated
@ConfigurationProperties(prefix = "rrhh.planilla")
@Getter @Setter
public class PlanillaProperties {
    @Positive
    private int rmv;
    @Positive
    private int uit;

    @NotBlank
    private String rucEmpleador;

    public PlanillaProperties() {
    }

    public PlanillaProperties(int rmv, int uit, String rucEmpleador) {
        this.rmv = rmv;
        this.uit = uit;
        this.rucEmpleador = rucEmpleador;
    }
}
