package com.clinica.maestro.entity.clinico;
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

import java.math.BigDecimal;

@Entity
@Table(name = "tb_tipos_habitacion")
@AttributeOverride(name = "createdAt", column = @Column(name = "thab_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "thab_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "thab_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoHabitacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "thab_id")
    private Long id;

    @Column(name = "thab_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "thab_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "thab_tarifa_base", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaBase;

    @Column(name = "thab_capacidad", nullable = false)
    private Integer capacidad;
}
