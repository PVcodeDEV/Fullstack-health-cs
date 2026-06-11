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
@Table(name = "tb_vias_administracion")
@AttributeOverride(name = "createdAt", column = @Column(name = "via_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "via_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "via_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ViaAdministracion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "via_id")
    private Long id;

    @Column(name = "via_codigo", nullable = false, unique = true, length = 10)
    private String codigo;

    @Column(name = "via_nombre", nullable = false, length = 100)
    private String nombre;
}
