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
@Table(name = "tb_notas_evolucion")
@AttributeOverride(name = "createdAt", column = @Column(name = "nota_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "nota_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "nota_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NotaEvolucion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "nota_id")
    private Long id;

    @Column(name = "nota_hospitalizacion_id", nullable = false)
    private Long hospitalizacionId;

    @Column(name = "nota_fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(name = "nota_usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "nota_descripcion", nullable = false, columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "nota_plan", columnDefinition = "TEXT")
    private String plan;

    @Column(name = "nota_tipo", nullable = false, length = 20)
    private String tipo = "EVOLUCION";

    @Column(name = "nota_signos_vitales", columnDefinition = "TEXT")
    private String signosVitales;
}
