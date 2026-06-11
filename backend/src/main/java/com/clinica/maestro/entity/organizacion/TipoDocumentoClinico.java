package com.clinica.maestro.entity.organizacion;
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
@Table(name = "tb_tipos_documento_clinico")
@AttributeOverride(name = "createdAt", column = @Column(name = "tdc_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tdc_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tdc_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoClinico extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tdc_id")
    private Integer id;

    @Column(name = "tdc_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "tdc_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tdc_requiere_firma", nullable = false)
    private Boolean requiereFirma = false;
}
