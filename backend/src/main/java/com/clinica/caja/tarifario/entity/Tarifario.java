package com.clinica.caja.tarifario.entity;

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
import lombok.ToString;

@Entity
@Table(name = "caja_tarifario")
@AttributeOverride(name = "createdAt", column = @Column(name = "tar_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tar_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tar_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Tarifario extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tar_id")
    private Long id;

    @Column(name = "tar_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tar_descripcion", length = 255)
    private String descripcion;

    @Column(name = "tar_aseguradora_id")
    private Long aseguradoraId;
}
