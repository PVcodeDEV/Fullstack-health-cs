package com.clinica.caja.comprobante.entity;

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

import java.time.LocalDateTime;

/**
 * Audit trail for comprobante reprint actions.
 * Logs every reprint request with user, timestamp, and origin IP.
 */
@Entity
@Table(name = "caja_reprint_log")
@AttributeOverride(name = "createdAt", column = @Column(name = "rep_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "rep_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "rep_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
public class ReprintLog extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "rep_id")
    private Long id;

    @Column(name = "rep_comprobante_id", nullable = false)
    private Long comprobanteId;

    @Column(name = "rep_usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "rep_fecha", nullable = false)
    private LocalDateTime fecha;

    @Column(name = "rep_ip_origen", length = 45)
    private String ipOrigen;
}
