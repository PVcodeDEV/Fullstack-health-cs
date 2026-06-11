package com.clinica.clinica.cama.entity;

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
@Table(name = "tb_habitaciones")
@AttributeOverride(name = "createdAt", column = @Column(name = "hab_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "hab_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "hab_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Habitacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hab_id")
    private Long id;

    @Column(name = "hab_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Column(name = "hab_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "hab_tipo_habitacion_id", nullable = false)
    private Long tipoHabitacionId;

    @Column(name = "hab_piso")
    private Integer piso;

    @Column(name = "hab_capacidad", nullable = false)
    private Integer capacidad = 1;
}
