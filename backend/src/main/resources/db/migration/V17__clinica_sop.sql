-- =============================================================================
-- V17: Clinica — Sala de Operaciones (SOP)
-- =============================================================================

-- tb_reportes_quirurgicos
CREATE TABLE tb_reportes_quirurgicos (
    sop_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sop_hospitalizacion_id  BIGINT        NOT NULL UNIQUE,
    sop_fecha_cirugia       DATE          NOT NULL,
    sop_hora_inicio         TIME          NOT NULL,
    sop_hora_fin            TIME,
    sop_cirujano_id         BIGINT        NOT NULL,
    sop_anestesiologo_id    BIGINT,
    sop_diagnostico_pre     TEXT,
    sop_diagnostico_post    TEXT,
    sop_procedimiento_realizado TEXT   NOT NULL,
    sop_hallazgos           TEXT,
    sop_complicaciones      TEXT,
    sop_medico_id           BIGINT        NOT NULL,
    sop_estado              VARCHAR(20)   NOT NULL DEFAULT 'BORRADOR',
    sop_created_at          TIMESTAMP     NOT NULL,
    sop_updated_at          TIMESTAMP,
    sop_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_sop_hospitalizacion FOREIGN KEY (sop_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_sop_cirujano FOREIGN KEY (sop_cirujano_id)
        REFERENCES tb_medicos(med_id),
    CONSTRAINT fk_sop_anestesiologo FOREIGN KEY (sop_anestesiologo_id)
        REFERENCES tb_medicos(med_id),
    CONSTRAINT fk_sop_medico FOREIGN KEY (sop_medico_id)
        REFERENCES tb_medicos(med_id),
    CONSTRAINT ck_sop_estado CHECK (sop_estado IN ('BORRADOR', 'COMPLETADO'))
);

-- tb_registros_urpa
CREATE TABLE tb_registros_urpa (
    urpa_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    urpa_reporte_id          BIGINT        NOT NULL,
    urpa_fecha_ingreso_urpa  TIMESTAMP     NOT NULL,
    urpa_fecha_salida_urpa   TIMESTAMP,
    urpa_condicion_ingreso   VARCHAR(255),
    urpa_condicion_salida    VARCHAR(255),
    urpa_escala_aldrete_ingreso INTEGER   NOT NULL CHECK (urpa_escala_aldrete_ingreso BETWEEN 0 AND 10),
    urpa_escala_aldrete_salida  INTEGER   CHECK (urpa_escala_aldrete_salida BETWEEN 0 AND 10),
    urpa_observaciones       TEXT,
    urpa_created_at          TIMESTAMP     NOT NULL,
    urpa_updated_at          TIMESTAMP,
    urpa_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_urpa_reporte FOREIGN KEY (urpa_reporte_id)
        REFERENCES tb_reportes_quirurgicos(sop_id)
);

-- Indexes
CREATE INDEX idx_sop_hospitalizacion ON tb_reportes_quirurgicos(sop_hospitalizacion_id);
CREATE INDEX idx_sop_estado ON tb_reportes_quirurgicos(sop_estado);
CREATE INDEX idx_urpa_reporte ON tb_registros_urpa(urpa_reporte_id);
