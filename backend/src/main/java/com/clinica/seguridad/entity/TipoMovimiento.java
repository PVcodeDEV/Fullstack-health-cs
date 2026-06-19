package com.clinica.seguridad.entity;

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
@Table(name = "tb_tipos_movimiento")
@AttributeOverride(name = "createdAt", column = @Column(name = "tim_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tim_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tim_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TipoMovimiento extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tim_id")
    private Long id;

    @Column(name = "tim_codigo", nullable = false, unique = true, length = 30)
    private String codigo;

    @Column(name = "tim_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "tim_modulo", nullable = false, length = 50)
    private String modulo;

    @Column(name = "tim_descripcion", length = 255)
    private String descripcion;
}
