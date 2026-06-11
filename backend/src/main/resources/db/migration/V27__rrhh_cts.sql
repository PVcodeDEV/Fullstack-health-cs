-- ============================================================================
-- V27: CTS (Compensación por Tiempo de Servicios) — Pequeña Empresa REMYPE
-- ============================================================================

CREATE TABLE tb_depositos_cts (
    dct_id                          BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    dct_periodo_planilla_id         BIGINT       NOT NULL,
    dct_trabajador_id               BIGINT       NOT NULL,
    dct_contrato_id                 BIGINT,
    dct_semestre                    VARCHAR(20)  NOT NULL,
    dct_dias_computables            INTEGER      NOT NULL,
    dct_remuneracion_computable     NUMERIC(10,2),
    dct_promedio_gratificacion      NUMERIC(10,2),
    dct_promedio_bonificacion       NUMERIC(10,2),
    dct_monto_cts                   NUMERIC(10,2),
    dct_estado                      VARCHAR(20)  NOT NULL DEFAULT 'CALCULADO',
    dct_activo                      BOOLEAN      NOT NULL DEFAULT TRUE,
    dct_created_at                  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    dct_updated_at                  TIMESTAMP,
    CONSTRAINT fk_dct_periodo FOREIGN KEY (dct_periodo_planilla_id) REFERENCES tb_periodos_planilla(ppl_id),
    CONSTRAINT fk_dct_trabajador FOREIGN KEY (dct_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_dct_contrato FOREIGN KEY (dct_contrato_id) REFERENCES tb_contratos(con_id),
    CONSTRAINT uq_dct_periodo_trabajador UNIQUE (dct_periodo_planilla_id, dct_trabajador_id)
);

CREATE INDEX idx_dct_periodo ON tb_depositos_cts(dct_periodo_planilla_id);
CREATE INDEX idx_dct_trabajador ON tb_depositos_cts(dct_trabajador_id);

-- Add CTS concepts
INSERT INTO tb_conceptos_planilla (cpl_codigo, cpl_nombre, cpl_tipo, cpl_orden) VALUES
    ('CTS_DEPOSITO', 'CTS Depósito', 'INGRESO', 4),
    ('CTS_GRATIF_PROMEDIO', 'CTS Promedio Gratificación', 'INGRESO', 5),
    ('CTS_BONIF_PROMEDIO', 'CTS Promedio Bonificación', 'INGRESO', 6);
