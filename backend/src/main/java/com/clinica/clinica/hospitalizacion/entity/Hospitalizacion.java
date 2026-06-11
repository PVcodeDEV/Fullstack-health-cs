package com.clinica.clinica.hospitalizacion.entity;

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
@Table(name = "tb_hospitalizaciones")
@AttributeOverride(name = "createdAt", column = @Column(name = "hosp_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "hosp_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "hosp_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Hospitalizacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hosp_id")
    private Long id;

    @Column(name = "hosp_solicitud_id", nullable = false, unique = true)
    private Long solicitudId;

    @Column(name = "hosp_cuenta_id", nullable = false)
    private Long cuentaId;

    @Column(name = "hosp_paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "hosp_cama_id", nullable = false)
    private Long camaId;

    @Column(name = "hosp_fecha_ingreso", nullable = false)
    private LocalDateTime fechaIngreso;

    @Column(name = "hosp_fecha_alta")
    private LocalDateTime fechaAlta;

    @Column(name = "hosp_estado", nullable = false, length = 20)
    private String estado = "HOSPITALIZADO";

    @Column(name = "hosp_tiene_reporte_operatorio", nullable = false)
    private Boolean tieneReporteOperatorio = false;
}
