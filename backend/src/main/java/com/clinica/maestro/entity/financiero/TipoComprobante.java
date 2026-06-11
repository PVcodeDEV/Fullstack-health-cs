package com.clinica.maestro.entity.financiero;
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
@Table(name = "tb_tipos_comprobante")
@AttributeOverride(name = "createdAt", column = @Column(name = "tcomp_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tcomp_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tcomp_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoComprobante extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tcomp_id")
    private Integer id;

    @Column(name = "tcomp_codigo_sunat", nullable = false, unique = true, length = 2)
    private String codigoSunat;

    @Column(name = "tcomp_nombre", nullable = false, length = 100)
    private String nombre;
}
