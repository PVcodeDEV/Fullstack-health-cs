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
@Table(name = "tb_altas_medicas")
@AttributeOverride(name = "createdAt", column = @Column(name = "alt_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "alt_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "alt_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AltaMedica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "alt_id")
    private Long id;

    @Column(name = "alt_hospitalizacion_id", nullable = false, unique = true)
    private Long hospitalizacionId;

    @Column(name = "alt_fecha_alta", nullable = false)
    private LocalDateTime fechaAlta;

    @Column(name = "alt_tipo_alta", nullable = false, length = 20)
    private String tipoAlta;

    @Column(name = "alt_diagnostico_final", nullable = false, columnDefinition = "TEXT")
    private String diagnosticoFinal;

    @Column(name = "alt_medico_id", nullable = false)
    private Long medicoId;

    @Column(name = "alt_observaciones", columnDefinition = "TEXT")
    private String observaciones;
}
