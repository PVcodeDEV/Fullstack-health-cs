-- ============================================================================
-- V29: PLAME / T-Registro (SUNAT electronic payroll filing)
-- ============================================================================

CREATE TABLE tb_tregistro_eventos (
    tre_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    tre_trabajador_id           BIGINT       NOT NULL,
    tre_contrato_id             BIGINT,
    tre_tipo_evento             VARCHAR(20)  NOT NULL,
    tre_fecha_evento            DATE         NOT NULL,
    tre_periodo_planilla_id     BIGINT,
    tre_detalle_json            TEXT,
    tre_estado                  VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    tre_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    tre_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tre_updated_at              TIMESTAMP,
    CONSTRAINT fk_tre_trabajador FOREIGN KEY (tre_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_tre_contrato FOREIGN KEY (tre_contrato_id) REFERENCES tb_contratos(con_id),
    CONSTRAINT fk_tre_periodo FOREIGN KEY (tre_periodo_planilla_id) REFERENCES tb_periodos_planilla(ppl_id)
);

CREATE INDEX idx_tre_trabajador ON tb_tregistro_eventos(tre_trabajador_id);
CREATE INDEX idx_tre_periodo ON tb_tregistro_eventos(tre_periodo_planilla_id);
CREATE INDEX idx_tre_tipo ON tb_tregistro_eventos(tre_tipo_evento);

CREATE TABLE tb_archivos_planilla (
    arp_id                      BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    arp_periodo_planilla_id     BIGINT       NOT NULL,
    arp_tipo                    VARCHAR(20)  NOT NULL,
    arp_contenido               TEXT         NOT NULL,
    arp_hash                    VARCHAR(64)  NOT NULL,
    arp_generado_por            VARCHAR(100),
    arp_activo                  BOOLEAN      NOT NULL DEFAULT TRUE,
    arp_created_at              TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    arp_updated_at              TIMESTAMP,
    CONSTRAINT fk_arp_periodo FOREIGN KEY (arp_periodo_planilla_id) REFERENCES tb_periodos_planilla(ppl_id)
);

CREATE INDEX idx_arp_periodo ON tb_archivos_planilla(arp_periodo_planilla_id);
CREATE INDEX idx_arp_tipo ON tb_archivos_planilla(arp_tipo);
