package com.clinica.maestro.entity.identidad;
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
@Table(name = "tb_estados_civil")
@AttributeOverride(name = "createdAt", column = @Column(name = "esc_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "esc_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "esc_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EstadoCivil extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "esc_id")
    private Long id;

    @Column(name = "esc_codigo_reniec", nullable = false, unique = true, length = 3)
    private String codigoReniec;

    @Column(name = "esc_nombre", nullable = false, length = 50)
    private String nombre;
}
