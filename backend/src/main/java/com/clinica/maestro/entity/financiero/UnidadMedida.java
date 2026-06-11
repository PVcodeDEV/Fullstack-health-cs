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
@Table(name = "tb_unidades_medida")
@AttributeOverride(name = "createdAt", column = @Column(name = "umed_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "umed_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "umed_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UnidadMedida extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "umed_id")
    private Integer id;

    @Column(name = "umed_codigo_sunat", nullable = false, unique = true, length = 5)
    private String codigoSunat;

    @Column(name = "umed_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "umed_abreviatura", length = 10)
    private String abreviatura;
}
