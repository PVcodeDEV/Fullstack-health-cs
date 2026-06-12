package com.clinica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.entidad")
public record EntidadProperties(
    @DefaultValue("https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm")
    String sunatRucUrl,

    @DefaultValue("10000")
    int connectTimeout,

    @DefaultValue("10000")
    int readTimeout
) {}
