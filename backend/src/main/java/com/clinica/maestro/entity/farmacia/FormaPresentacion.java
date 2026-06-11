package com.clinica.maestro.entity.farmacia;
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
@Table(name = "tb_formas_presentacion")
@AttributeOverride(name = "createdAt", column = @Column(name = "fpre_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "fpre_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "fpre_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormaPresentacion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fpre_id")
    private Long id;

    @Column(name = "fpre_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "fpre_nombre", nullable = false, length = 100)
    private String nombre;
}
