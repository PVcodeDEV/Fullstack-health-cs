-- ============================================================================
-- V28: Vacaciones Legales (Pequeña Empresa REMYPE)
-- ============================================================================

CREATE TABLE tb_vacaciones_record (
    vcr_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    vcr_trabajador_id           BIGINT       NOT NULL,
    vcr_contrato_id             BIGINT,
    vcr_fecha_inicio            DATE         NOT NULL,
    vcr_fecha_fin               DATE         NOT NULL,
    vcr_dias_derecho            INTEGER      NOT NULL DEFAULT 15,
    vcr_dias_reduccion          INTEGER      NOT NULL DEFAULT 0,
    vcr_dias_pendientes         NUMERIC(5,2) NOT NULL,
    vcr_estado                  VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    vcr_fecha_expiracion        DATE         NOT NULL,
    vcr_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    vcr_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vcr_updated_at              TIMESTAMP,
    CONSTRAINT fk_vcr_trabajador FOREIGN KEY (vcr_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_vcr_contrato FOREIGN KEY (vcr_contrato_id) REFERENCES tb_contratos(con_id),
    CONSTRAINT uq_vcr_trabajador_periodo UNIQUE (vcr_trabajador_id, vcr_fecha_inicio)
);

CREATE INDEX idx_vcr_trabajador ON tb_vacaciones_record(vcr_trabajador_id);
CREATE INDEX idx_vcr_estado ON tb_vacaciones_record(vcr_estado);

CREATE TABLE tb_vacaciones_goces (
    vgo_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    vgo_record_id               BIGINT       NOT NULL,
    vgo_fecha_inicio            DATE         NOT NULL,
    vgo_fecha_fin               DATE         NOT NULL,
    vgo_dias                    INTEGER      NOT NULL,
    vgo_remuneracion            NUMERIC(10,2),
    vgo_estado                  VARCHAR(20)  NOT NULL DEFAULT 'PROGRAMADO',
    vgo_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    vgo_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vgo_updated_at              TIMESTAMP,
    CONSTRAINT fk_vgo_record FOREIGN KEY (vgo_record_id) REFERENCES tb_vacaciones_record(vcr_id),
    CONSTRAINT ck_vgo_dias CHECK (vgo_dias >= 7)
);

CREATE INDEX idx_vgo_record ON tb_vacaciones_goces(vgo_record_id);
CREATE INDEX idx_vgo_estado ON tb_vacaciones_goces(vgo_estado);
