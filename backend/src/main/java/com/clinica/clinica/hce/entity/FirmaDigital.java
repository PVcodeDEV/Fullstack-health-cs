package com.clinica.clinica.hce.entity;

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

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_firmas_digitales")
@AttributeOverride(name = "createdAt", column = @Column(name = "fir_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "fir_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "fir_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FirmaDigital extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "fir_id")
    private Long id;

    @Column(name = "fir_documento_id", nullable = false)
    private Long documentoId;

    @Column(name = "fir_usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "fir_fecha_firma", nullable = false)
    private LocalDateTime fechaFirma;

    @Column(name = "fir_hash_sha256", nullable = false, length = 64)
    private String hashSha256;

    @Column(name = "fir_ip_origen", nullable = false, length = 45)
    private String ipOrigen;
}
