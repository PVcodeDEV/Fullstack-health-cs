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

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_solicitudes_medicamento")
@AttributeOverride(name = "createdAt", column = @Column(name = "smed_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "smed_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "smed_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SolicitudMedicamento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "smed_id")
    private Long id;

    @Column(name = "smed_hospitalizacion_id", nullable = false)
    private Long hospitalizacionId;

    @Column(name = "smed_medicamento_id", nullable = false)
    private Long medicamentoId;

    @Column(name = "smed_dosis", nullable = false, length = 50)
    private String dosis;

    @Column(name = "smed_frecuencia", nullable = false, length = 50)
    private String frecuencia;

    @Column(name = "smed_via_administracion_id")
    private Long viaAdministracionId;

    @Column(name = "smed_fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "smed_fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "smed_estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "smed_usuario_id", nullable = false)
    private Long usuarioId;
}
