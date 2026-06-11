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
@Table(name = "tb_roles")
@AttributeOverride(name = "createdAt", column = @Column(name = "rol_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "rol_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "rol_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Rol extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rol_id")
    private Long id;

    @Column(name = "rol_codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "rol_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "rol_descripcion", length = 255)
    private String descripcion;
}
