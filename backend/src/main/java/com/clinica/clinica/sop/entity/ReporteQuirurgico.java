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

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "tb_reportes_quirurgicos")
@AttributeOverride(name = "createdAt", column = @Column(name = "sop_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "sop_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "sop_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReporteQuirurgico extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "sop_id")
    private Long id;

    @Column(name = "sop_hospitalizacion_id", nullable = false, unique = true)
    private Long hospitalizacionId;

    @Column(name = "sop_fecha_cirugia", nullable = false)
    private LocalDate fechaCirugia;

    @Column(name = "sop_hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "sop_hora_fin")
    private LocalTime horaFin;

    @Column(name = "sop_cirujano_id", nullable = false)
    private Long cirujanoId;

    @Column(name = "sop_anestesiologo_id")
    private Long anestesiologoId;

    @Column(name = "sop_diagnostico_pre", columnDefinition = "TEXT")
    private String diagnosticoPre;

    @Column(name = "sop_diagnostico_post", columnDefinition = "TEXT")
    private String diagnosticoPost;

    @Column(name = "sop_procedimiento_realizado", nullable = false, columnDefinition = "TEXT")
    private String procedimientoRealizado;

    @Column(name = "sop_hallazgos", columnDefinition = "TEXT")
    private String hallazgos;

    @Column(name = "sop_complicaciones", columnDefinition = "TEXT")
    private String complicaciones;

    @Column(name = "sop_medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "sop_estado", nullable = false, length = 20)
    private String estado = "BORRADOR";
}
