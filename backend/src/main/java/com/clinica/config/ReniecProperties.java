package com.clinica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.reniec")
public record ReniecProperties(
    @DefaultValue("https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm")
    String sunatUrl,
    String secureUrl,
    String secureToken,
    @DefaultValue("false")
    boolean secureEnabled
) {}
