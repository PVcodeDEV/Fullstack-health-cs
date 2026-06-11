-- =============================================================================
-- V18: Clinica — Historia Clínica Electrónica (HCE)
-- =============================================================================

-- tb_documentos_clinicos
CREATE TABLE tb_documentos_clinicos (
    hce_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hce_paciente_id         BIGINT        NOT NULL,
    hce_hospitalizacion_id  BIGINT,
    hce_documento_original_id BIGINT,
    hce_tipo_documento      VARCHAR(30)   NOT NULL,
    hce_contenido           BYTEA,
    hce_tamano_bytes        BIGINT        NOT NULL DEFAULT 0,
    hce_medico_id           BIGINT,
    hce_created_at          TIMESTAMP     NOT NULL,
    hce_updated_at          TIMESTAMP,
    hce_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_hce_paciente FOREIGN KEY (hce_paciente_id)
        REFERENCES tb_pacientes(pac_id),
    CONSTRAINT fk_hce_hospitalizacion FOREIGN KEY (hce_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_hce_original FOREIGN KEY (hce_documento_original_id)
        REFERENCES tb_documentos_clinicos(hce_id),
    CONSTRAINT fk_hce_medico FOREIGN KEY (hce_medico_id)
        REFERENCES tb_medicos(med_id),
    CONSTRAINT ck_hce_tipo CHECK (hce_tipo_documento IN (
        'FILIACION', 'CONSENTIMIENTO', 'HC', 'EVOLUCION',
        'REPORTE_OPERATORIO', 'EPICRISIS', 'KARDEX', 'NOTA_ENFERMERIA'
    ))
);

-- tb_firmas_digitales
CREATE TABLE tb_firmas_digitales (
    fir_id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fir_documento_id    BIGINT        NOT NULL,
    fir_usuario_id      BIGINT        NOT NULL,
    fir_fecha_firma     TIMESTAMP     NOT NULL,
    fir_hash_sha256     VARCHAR(64)   NOT NULL,
    fir_ip_origen       VARCHAR(45)   NOT NULL,
    fir_created_at      TIMESTAMP     NOT NULL,
    fir_updated_at      TIMESTAMP,
    fir_activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_fir_documento FOREIGN KEY (fir_documento_id)
        REFERENCES tb_documentos_clinicos(hce_id),
    CONSTRAINT fk_fir_usuario FOREIGN KEY (fir_usuario_id)
        REFERENCES tb_usuarios(usu_id)
);

-- Indexes
CREATE INDEX idx_hce_paciente ON tb_documentos_clinicos(hce_paciente_id);
CREATE INDEX idx_hce_hospitalizacion ON tb_documentos_clinicos(hce_hospitalizacion_id);
CREATE INDEX idx_hce_tipo ON tb_documentos_clinicos(hce_tipo_documento);
CREATE INDEX idx_hce_original ON tb_documentos_clinicos(hce_documento_original_id);
CREATE INDEX idx_fir_documento ON tb_firmas_digitales(fir_documento_id);
CREATE INDEX idx_fir_usuario ON tb_firmas_digitales(fir_usuario_id);
