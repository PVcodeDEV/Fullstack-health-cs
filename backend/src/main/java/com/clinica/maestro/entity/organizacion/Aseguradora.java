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
@Table(name = "tb_aseguradoras")
@AttributeOverride(name = "createdAt", column = @Column(name = "aseg_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "aseg_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "aseg_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Aseguradora extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aseg_id")
    private Integer id;

    @Column(name = "aseg_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "aseg_nombre", nullable = false, length = 150)
    private String nombre;

    @Column(name = "aseg_tipo", nullable = false, length = 10)
    private String tipo;

    @Column(name = "aseg_contrato_vigente", nullable = false)
    private Boolean contratoVigente = true;
}
