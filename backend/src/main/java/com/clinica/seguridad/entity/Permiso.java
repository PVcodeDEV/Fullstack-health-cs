package com.clinica.seguridad.entity;

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
@Table(name = "tb_permisos")
@AttributeOverride(name = "createdAt", column = @Column(name = "per_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "per_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "per_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Permiso extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "per_id")
    private Long id;

    @Column(name = "per_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "per_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "per_modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "per_descripcion", length = 255)
    private String descripcion;
}
