-- ============================================================================
-- V26: Gratificación Legal (Pequeña Empresa REMYPE)
-- ============================================================================

CREATE TABLE tb_gratificaciones (
    gra_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    gra_periodo_planilla_id     BIGINT       NOT NULL,
    gra_trabajador_id           BIGINT       NOT NULL,
    gra_contrato_id             BIGINT,
    gra_semestre                VARCHAR(20)  NOT NULL,
    gra_meses_computables       INTEGER      NOT NULL,
    gra_remuneracion_computable NUMERIC(10,2),
    gra_gratificacion           NUMERIC(10,2),
    gra_bonificacion_extraordinaria NUMERIC(10,2),
    gra_total                   NUMERIC(10,2),
    gra_estado                  VARCHAR(20)  NOT NULL DEFAULT 'CALCULADO',
    gra_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    gra_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gra_updated_at              TIMESTAMP,
    CONSTRAINT fk_gra_periodo FOREIGN KEY (gra_periodo_planilla_id) REFERENCES tb_periodos_planilla(ppl_id),
    CONSTRAINT fk_gra_trabajador FOREIGN KEY (gra_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_gra_contrato FOREIGN KEY (gra_contrato_id) REFERENCES tb_contratos(con_id),
    CONSTRAINT uq_gra_periodo_trabajador UNIQUE (gra_periodo_planilla_id, gra_trabajador_id)
);

CREATE INDEX idx_gra_periodo ON tb_gratificaciones(gra_periodo_planilla_id);
CREATE INDEX idx_gra_trabajador ON tb_gratificaciones(gra_trabajador_id);

-- Add gratificación concepts
INSERT INTO tb_conceptos_planilla (cpl_codigo, cpl_nombre, cpl_tipo, cpl_orden) VALUES
    ('GRATIFICACION', 'Gratificación Legal', 'INGRESO', 3),
    ('BONIF_EXTRAORDINARIA', 'Bonificación Extraordinaria', 'APORTE', 21);
