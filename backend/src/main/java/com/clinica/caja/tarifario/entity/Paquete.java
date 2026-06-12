package com.clinica.caja.tarifario.entity;

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
import lombok.ToString;

import java.math.BigDecimal;

@Entity
@Table(name = "caja_paquete")
@AttributeOverride(name = "createdAt", column = @Column(name = "paq_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "paq_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "paq_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Paquete extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "paq_id")
    private Long id;

    @Column(name = "paq_codigo", nullable = false, unique = true, length = 50)
    private String codigo;

    @Column(name = "paq_nombre", nullable = false, length = 200)
    private String nombre;

    @Column(name = "paq_descripcion", length = 500)
    private String descripcion;

    @Column(name = "paq_precio_total", nullable = false, precision = 10, scale = 2)
    private BigDecimal precioTotal;
}
