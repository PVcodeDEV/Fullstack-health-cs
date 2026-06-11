-- ============================================================================
-- V25: Payroll tables — periods, headers, detail lines
-- ============================================================================

CREATE TABLE tb_periodos_planilla (
    ppl_id              BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    ppl_anio            INTEGER      NOT NULL,
    ppl_mes             INTEGER      NOT NULL,
    ppl_fecha_inicio    DATE         NOT NULL,
    ppl_fecha_fin       DATE         NOT NULL,
    ppl_estado          VARCHAR(20)  NOT NULL DEFAULT 'ABIERTO',
    ppl_activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    ppl_created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ppl_updated_at      TIMESTAMP,
    CONSTRAINT uq_ppl_anio_mes UNIQUE (ppl_anio, ppl_mes),
    CONSTRAINT ck_ppl_mes CHECK (ppl_mes >= 1 AND ppl_mes <= 12),
    CONSTRAINT ck_ppl_estado CHECK (ppl_estado IN ('ABIERTO', 'CERRADO', 'ANULADO'))
);

CREATE TABLE tb_planillas (
    pla_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    pla_periodo_planilla_id     BIGINT       NOT NULL,
    pla_fecha_liquidacion       DATE,
    pla_total_ingresos          NUMERIC(10,2) NOT NULL DEFAULT 0,
    pla_total_descuentos        NUMERIC(10,2) NOT NULL DEFAULT 0,
    pla_total_aportes           NUMERIC(10,2) NOT NULL DEFAULT 0,
    pla_total_neto              NUMERIC(10,2) NOT NULL DEFAULT 0,
    pla_cantidad_trabajadores   INTEGER      NOT NULL DEFAULT 0,
    pla_estado                  VARCHAR(20)  NOT NULL DEFAULT 'BORRADOR',
    pla_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    pla_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pla_updated_at              TIMESTAMP,
    CONSTRAINT fk_pla_periodo FOREIGN KEY (pla_periodo_planilla_id) REFERENCES tb_periodos_planilla(ppl_id),
    CONSTRAINT ck_pla_estado CHECK (pla_estado IN ('BORRADOR', 'LIQUIDADO'))
);

CREATE INDEX idx_pla_periodo ON tb_planillas(pla_periodo_planilla_id);

CREATE TABLE tb_planilla_detalles (
    pde_id                  BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    pde_planilla_id         BIGINT       NOT NULL,
    pde_trabajador_id       BIGINT       NOT NULL,
    pde_contrato_id         BIGINT,
    pde_sueldo_base         NUMERIC(10,2),
    pde_asignacion_familiar NUMERIC(10,2) NOT NULL DEFAULT 0,
    pde_dias_laborados      INTEGER      NOT NULL DEFAULT 30,
    pde_total_ingresos      NUMERIC(10,2) NOT NULL DEFAULT 0,
    pde_total_descuentos    NUMERIC(10,2) NOT NULL DEFAULT 0,
    pde_total_aportes       NUMERIC(10,2) NOT NULL DEFAULT 0,
    pde_neto                NUMERIC(10,2) NOT NULL DEFAULT 0,
    pde_conceptos_json      TEXT,
    pde_activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    pde_created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pde_updated_at          TIMESTAMP,
    CONSTRAINT fk_pde_planilla FOREIGN KEY (pde_planilla_id) REFERENCES tb_planillas(pla_id),
    CONSTRAINT fk_pde_trabajador FOREIGN KEY (pde_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_pde_contrato FOREIGN KEY (pde_contrato_id) REFERENCES tb_contratos(con_id)
);

CREATE INDEX idx_pde_planilla ON tb_planilla_detalles(pde_planilla_id);
CREATE INDEX idx_pde_trabajador ON tb_planilla_detalles(pde_trabajador_id);
