package com.clinica.clinica.paciente.entity;

import com.clinica.maestro.entity.BaseEntity;
import com.clinica.persona.entity.Persona;
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
@Table(name = "tb_pacientes")
@AttributeOverride(name = "createdAt", column = @Column(name = "pac_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "pac_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "pac_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paciente extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "pac_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pac_persona_id", nullable = false, unique = true)
    private Persona persona;

    @Column(name = "pac_tipo_paciente", nullable = false, length = 20)
    private String tipoPaciente = "PARTICULAR";

    @Column(name = "pac_nro_historia_clinica", unique = true, length = 20)
    private String nroHistoriaClinica;

    @Column(name = "pac_grupo_sanguineo", length = 5)
    private String grupoSanguineo;

    @Column(name = "pac_alergias", columnDefinition = "TEXT")
    private String alergias;

    @Column(name = "pac_contacto_emergencia_nombre", length = 200)
    private String contactoEmergenciaNombre;

    @Column(name = "pac_contacto_emergencia_telefono", length = 20)
    private String contactoEmergenciaTelefono;
}
