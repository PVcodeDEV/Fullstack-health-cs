package com.clinica.caja.tipocambio.entity;

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
import java.time.LocalDate;

@Entity
@Table(name = "caja_tipo_cambio")
@AttributeOverride(name = "createdAt", column = @Column(name = "tcam_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "tcam_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "tcam_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class TipoCambio extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "tcam_id")
    private Long id;

    @Column(name = "tcam_moneda_origen", nullable = false, length = 3)
    private String monedaOrigen;

    @Column(name = "tcam_moneda_destino", nullable = false, length = 3)
    private String monedaDestino;

    @Column(name = "tcam_tipo_cambio", nullable = false, precision = 10, scale = 4)
    private BigDecimal tipoCambio;

    @Column(name = "tcam_fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "tcam_usuario_id", nullable = false)
    private Long usuarioId;
}
