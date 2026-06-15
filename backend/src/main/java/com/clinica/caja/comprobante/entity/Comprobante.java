package com.clinica.caja.comprobante.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * SUNAT-compliant electronic comprobante (invoice/credit note).
 * Denormalizes client data at issuance time for XML immutability.
 * Stores the generated UBL 2.1 XML as a CLOB.
 */
@Entity
@Table(name = "caja_comprobante")
@AttributeOverride(name = "createdAt", column = @Column(name = "com_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "com_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "com_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class Comprobante extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "com_id")
    private Long id;

    @Column(name = "com_tipo_comprobante_id", nullable = false)
    private Integer tipoComprobanteId;

    @Column(name = "com_serie", nullable = false, length = 4)
    private String serie = "001";

    @Column(name = "com_correlativo", nullable = false, length = 8)
    private String correlativo;

    @Column(name = "com_fecha_emision", nullable = false)
    private LocalDateTime fechaEmision;

    @Column(name = "com_tipo_doc_cliente", nullable = false, length = 2)
    private String tipoDocCliente;

    @Column(name = "com_num_doc_cliente", nullable = false, length = 20)
    @ToString.Exclude
    private String numDocCliente;

    @Column(name = "com_nombre_cliente", nullable = false, length = 255)
    @ToString.Exclude
    private String nombreCliente;

    @Column(name = "com_direccion_cliente", length = 255)
    @ToString.Exclude
    private String direccionCliente;

    @Column(name = "com_persona_id")
    private Long personaId;

    @Column(name = "com_empresa_id")
    private Long empresaId;

    @Column(name = "com_subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "com_igv", nullable = false, precision = 10, scale = 2)
    private BigDecimal igv;

    @Column(name = "com_total", nullable = false, precision = 10, scale = 2)
    @ToString.Exclude
    private BigDecimal total;

    @Column(name = "com_moneda", nullable = false, length = 3)
    private String moneda = "PEN";

    @Column(name = "com_liquidacion_id")
    private Long liquidacionId;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "com_xml_generado", columnDefinition = "TEXT")
    @ToString.Exclude
    private String xmlGenerado;

    @Column(name = "com_estado", nullable = false, length = 20)
    private String estado = "EMITIDO";

    @Column(name = "com_comprobante_original_id")
    private Long comprobanteOriginalId;

    @Column(name = "com_motivo", length = 500)
    private String motivo;
}
