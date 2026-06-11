package com.clinica.farmacia.almacen.entity;

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
@Table(name = "tb_almacenes")
@AttributeOverride(name = "createdAt", column = @Column(name = "alm_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "alm_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "alm_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Almacen extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alm_id")
    private Long id;

    @Column(name = "alm_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "alm_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "alm_ubicacion", length = 255)
    private String ubicacion;

    @Column(name = "alm_default", nullable = false)
    private Boolean defaultWarehouse = false;
}
