package com.clinica.rrhh.planillaplame.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.rrhh.planilla.entity.PeriodoPlanilla;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tb_archivos_planilla")
@AttributeOverride(name = "createdAt", column = @Column(name = "arp_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "arp_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "arp_activo"))
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
@ToString(onlyExplicitlyIncluded = true)
public class ArchivoPlanilla extends BaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "arp_id")
    @ToString.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arp_periodo_planilla_id", nullable = false)
    private PeriodoPlanilla periodoPlanilla;

    @Column(name = "arp_tipo", nullable = false, length = 20)
    @ToString.Include
    private String tipo;

    @Column(name = "arp_contenido", nullable = false, columnDefinition = "TEXT")
    private String contenido;

    @Column(name = "arp_hash", nullable = false, length = 64)
    private String hash;

    @Column(name = "arp_generado_por", length = 100)
    private String generadoPor;
}
