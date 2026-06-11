package com.clinica.clinica.admision.entity;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_cuentas")
@AttributeOverride(name = "createdAt", column = @Column(name = "cue_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cue_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cue_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cuenta extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cue_id")
    private Long id;

    @Column(name = "cue_paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "cue_paquete_quirurgico_id")
    private Long paqueteQuirurgicoId;

    @Column(name = "cue_tipo_habitacion_id")
    private Long tipoHabitacionId;

    @Column(name = "cue_fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura;

    @Column(name = "cue_estado", nullable = false, length = 20)
    private String estado = "ABIERTO";

    @Column(name = "cue_total_estimado", precision = 10, scale = 2)
    private BigDecimal totalEstimado;

    @Column(name = "cue_total_cargos", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCargos = BigDecimal.ZERO;

    @Column(name = "cue_pendiente_cobro", nullable = false)
    private Boolean pendienteCobro = false;
}
