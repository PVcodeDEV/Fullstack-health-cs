package com.clinica.clinica.hce.entity;

import com.clinica.maestro.entity.BaseEntity;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tb_documentos_clinicos")
@AttributeOverride(name = "createdAt", column = @Column(name = "hce_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "hce_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "hce_activo"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoClinico extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "hce_id")
    private Long id;

    @Column(name = "hce_paciente_id", nullable = false)
    private Long pacienteId;

    @Column(name = "hce_hospitalizacion_id")
    private Long hospitalizacionId;

    @Column(name = "hce_documento_original_id")
    private Long documentoOriginalId;

    @Column(name = "hce_tipo_documento", nullable = false, length = 30)
    private String tipoDocumento;

    @Lob
    @Column(name = "hce_contenido", columnDefinition = "BYTEA")
    private byte[] contenido;

    @Column(name = "hce_tamano_bytes", nullable = false)
    private Long tamanoBytes = 0L;

    @Column(name = "hce_medico_id")
    private Long medicoId;
}
