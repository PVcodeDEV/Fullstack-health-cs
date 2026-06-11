package com.clinica.clinica.sop.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_registros_urpa")
@AttributeOverride(name = "createdAt", column = @Column(name = "urpa_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "urpa_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "urpa_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class URPARegistro extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "urpa_id")
    private Long id;

    @Column(name = "urpa_reporte_id", nullable = false)
    private Long reporteId;

    @Column(name = "urpa_fecha_ingreso_urpa", nullable = false)
    private LocalDateTime fechaIngresoURPA;

    @Column(name = "urpa_fecha_salida_urpa")
    private LocalDateTime fechaSalidaURPA;

    @Column(name = "urpa_condicion_ingreso", length = 255)
    private String condicionIngreso;

    @Column(name = "urpa_condicion_salida", length = 255)
    private String condicionSalida;

    @Column(name = "urpa_escala_aldrete_ingreso", nullable = false)
    private Integer escalaAldreteIngreso;

    @Column(name = "urpa_escala_aldrete_salida")
    private Integer escalaAldreteSalida;

    @Column(name = "urpa_observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
