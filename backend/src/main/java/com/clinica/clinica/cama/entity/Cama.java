package com.clinica.clinica.cama.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_camas")
@AttributeOverride(name = "createdAt", column = @Column(name = "cama_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "cama_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "cama_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Cama extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cama_id")
    private Long id;

    @Column(name = "cama_habitacion_id", nullable = false)
    private Long habitacionId;

    @Column(name = "cama_codigo", nullable = false, unique = true, length = 20)
    private String codigo;

    @Enumerated(EnumType.STRING)
    @Column(name = "cama_estado", nullable = false, length = 20)
    private EstadoCama estado = EstadoCama.DISPONIBLE;

    // --- State machine transitions ---

    public void ocupar() {
        if (estado != EstadoCama.DISPONIBLE) {
            throw new IllegalStateException(
                "Solo camas DISPONIBLES pueden ser ocupadas. Estado actual: " + estado);
        }
        this.estado = EstadoCama.OCUPADO;
    }

    public void liberar() {
        if (estado != EstadoCama.OCUPADO) {
            throw new IllegalStateException(
                "Solo camas OCUPADAS pueden ser liberadas. Estado actual: " + estado);
        }
        this.estado = EstadoCama.DISPONIBLE;
    }

    public void ponerEnMantenimiento() {
        if (estado == EstadoCama.MANTENIMIENTO) {
            return; // already in maintenance, idempotent
        }
        if (estado == EstadoCama.OCUPADO) {
            throw new IllegalStateException(
                "No se puede poner en mantenimiento una cama OCUPADA. Debe liberarse primero.");
        }
        this.estado = EstadoCama.MANTENIMIENTO;
    }

    public void disponibilizar() {
        if (estado != EstadoCama.MANTENIMIENTO) {
            throw new IllegalStateException(
                "Solo camas en MANTENIMIENTO pueden volver a disponible. Estado actual: " + estado);
        }
        this.estado = EstadoCama.DISPONIBLE;
    }

    public boolean isDisponible() {
        return estado == EstadoCama.DISPONIBLE;
    }
}
