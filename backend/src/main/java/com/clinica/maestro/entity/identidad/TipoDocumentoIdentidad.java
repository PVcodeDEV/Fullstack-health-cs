package com.clinica.maestro.entity.identidad;
import com.clinica.maestro.entity.BaseEntity;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_tipos_documento_identidad")
@AttributeOverride(name = "createdAt", column = @Column(name = "tdi_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tdi_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tdi_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoIdentidad extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tdi_id")
    private Long id;

    @Column(name = "tdi_codigo_sunat", nullable = false, unique = true, length = 5)
    private String codigoSunat;

    @Column(name = "tdi_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tdi_longitud_minima", nullable = false)
    private Integer longitudMinima;

    @Column(name = "tdi_longitud_maxima", nullable = false)
    private Integer longitudMaxima;
}
