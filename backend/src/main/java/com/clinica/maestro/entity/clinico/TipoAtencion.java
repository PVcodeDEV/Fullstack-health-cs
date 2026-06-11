package com.clinica.maestro.entity.clinico;
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

@Entity
@Table(name = "tb_tipos_atencion")
@AttributeOverride(name = "createdAt", column = @Column(name = "tate_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tate_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tate_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoAtencion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tate_id")
    private Long id;

    @Column(name = "tate_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "tate_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tate_requiere_habitacion", nullable = false)
    private Boolean requiereHabitacion = false;
}
