package com.clinica.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * SUNAT emisor configuration for electronic invoice XML generation.
 * Properties prefixed with {@code app.caja.comprobante}.
 */
@ConfigurationProperties(prefix = "app.caja.comprobante")
public record CajaComprobanteProperties(

    @DefaultValue("20123456789")
    String emisorRuc,

    @DefaultValue("CLINICA EJEMPLO SAC")
    String emisorRazonSocial,

    @DefaultValue("AV. PRINCIPAL 123, LIMA")
    String emisorDireccion,

    @DefaultValue("LIMA")
    String emisorDepartamento,

    @DefaultValue("LIMA")
    String emisorProvincia,

    @DefaultValue("LIMA")
    String emisorDistrito,

    @DefaultValue("150101")
    String emisorUbigeo,

    @DefaultValue("-")
    String emisorUrbanizacion,

    @DefaultValue("01")
    String emisorTipoVia
) {}
