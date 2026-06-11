package com.clinica.clinica.medico.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.persona.entity.Persona;
import com.clinica.rrhh.trabajador.entity.Trabajador;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_medicos")
@AttributeOverride(name = "createdAt", column = @Column(name = "med_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "med_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "med_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Medico extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "med_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_persona_id", nullable = false, unique = true)
    private Persona persona;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "med_trabajador_id", unique = true)
    private Trabajador trabajador;

    @Deprecated
    @Column(name = "med_cmp", nullable = false, unique = true, length = 20)
    private String cmp;

    @Column(name = "med_especialidad_id")
    private Long especialidadId;

    @Column(name = "med_es_especialista", nullable = false)
    private Boolean esEspecialista = false;

    /**
     * Returns the medical license number (CMP).
     * Delegates to Trabajador.colegiatura when available.
     * Falls back to the deprecated med_cmp column for data created before V20.
     */
    public String getCmp() {
        if (trabajador != null && trabajador.getNroColegiatura() != null) {
            return trabajador.getNroColegiatura();
        }
        return this.cmp;
    }
}
