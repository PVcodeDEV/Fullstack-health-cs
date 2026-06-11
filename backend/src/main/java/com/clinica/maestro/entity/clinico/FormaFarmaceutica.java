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
@Table(name = "tb_formas_farmaceuticas")
@AttributeOverride(name = "createdAt", column = @Column(name = "ffar_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "ffar_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "ffar_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormaFarmaceutica extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ffar_id")
    private Long id;

    @Column(name = "ffar_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "ffar_nombre", nullable = false, length = 100)
    private String nombre;

    @Column(name = "ffar_requiere_preparacion", nullable = false)
    private Boolean requierePreparacion = false;
}
