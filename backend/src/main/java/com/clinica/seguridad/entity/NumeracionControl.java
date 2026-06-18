package com.clinica.seguridad.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_numeracion_control",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numc_entidad", "numc_serie", "numc_anio"}))
@AttributeOverride(name = "createdAt", column = @Column(name = "numc_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "numc_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "numc_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class NumeracionControl extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numc_id")
    private Long id;

    @Column(name = "numc_entidad", nullable = false, length = 50)
    private String entidad;

    @Column(name = "numc_serie", nullable = false, length = 10)
    private String serie;

    @Column(name = "numc_correlativo_actual", nullable = false)
    private Long correlativoActual = 0L;

    @Column(name = "numc_prefijo", length = 10)
    private String prefijo;

    @Column(name = "numc_longitud_correlativo", nullable = false)
    private int longitudCorrelativo = 6;

    @Column(name = "numc_anio", nullable = false)
    private int anio;
}
