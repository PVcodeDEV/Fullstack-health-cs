package com.clinica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

import java.math.BigDecimal;

/**
 * Configuration properties for caja session management.
 * Maps from {@code app.caja.sesion.*} in application.yml.
 * <p>
 * Future: tolerance may be overridden by tb_configuracion_api values.
 */
@ConfigurationProperties(prefix = "app.caja.sesion")
public record CajaSesionProperties(
    @DefaultValue("1.00")
    BigDecimal toleranciaDiferencia
) {}
