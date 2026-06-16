-- =============================================================================
-- V23: RRHH Pension — AFP catalog + historical rates + worker pension info
-- =============================================================================

-- 1. AFPs catalog (maestro)
CREATE TABLE tb_afps (
    afp_id          BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    afp_codigo      VARCHAR(20)  NOT NULL UNIQUE,
    afp_nombre      VARCHAR(100) NOT NULL,
    afp_descripcion VARCHAR(255),
    afp_activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    afp_created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    afp_updated_at  TIMESTAMP
);

INSERT INTO tb_afps (afp_codigo, afp_nombre) VALUES
    ('PRIMA', 'Prima AFP'),
    ('PROFUTURO', 'Profuturo AFP'),
    ('HABITAT', 'Habitat AFP'),
    ('INTEGRA', 'Integra AFP'),
    ('ONP', 'Oficina de Normalización Previsional');

-- 2. AFP historical rates
CREATE TABLE tb_afp_tasas_historicas (
    ath_id              BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    ath_afp_id          BIGINT       NOT NULL,
    ath_tipo_comision   VARCHAR(20),
    ath_tasa            NUMERIC(5,4) NOT NULL,
    ath_prima_seguro    NUMERIC(5,4) NOT NULL,
    ath_rentabilidad    NUMERIC(5,4),
    ath_vigencia_desde  DATE         NOT NULL,
    ath_vigencia_hasta  DATE,
    ath_activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    ath_created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    ath_updated_at      TIMESTAMP,
    CONSTRAINT fk_ath_afp FOREIGN KEY (ath_afp_id) REFERENCES tb_afps(afp_id)
);

CREATE UNIQUE INDEX idx_ath_afp_vigencia ON tb_afp_tasas_historicas(ath_afp_id, ath_vigencia_desde);

-- Seed current rates (2026)
INSERT INTO tb_afp_tasas_historicas (ath_afp_id, ath_tipo_comision, ath_tasa, ath_prima_seguro, ath_vigencia_desde)
SELECT afp_id, 'FLUJO', 1.8500, 0.6900, DATE '2026-01-01' FROM tb_afps WHERE afp_codigo = 'PRIMA'
UNION ALL SELECT afp_id, 'FLUJO', 1.7500, 0.6200, DATE '2026-01-01' FROM tb_afps WHERE afp_codigo = 'PROFUTURO'
UNION ALL SELECT afp_id, 'FLUJO', 1.6000, 0.6400, DATE '2026-01-01' FROM tb_afps WHERE afp_codigo = 'HABITAT'
UNION ALL SELECT afp_id, 'FLUJO', 1.6900, 0.6700, DATE '2026-01-01' FROM tb_afps WHERE afp_codigo = 'INTEGRA';

-- 3. Worker pension info (1:1 with Trabajador)
CREATE TABLE tb_informacion_pensionaria (
    inf_id                   BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    inf_trabajador_id        BIGINT       NOT NULL,
    inf_afp_id               BIGINT       NOT NULL,
    inf_cuspp                VARCHAR(16),
    inf_comision_tipo        VARCHAR(20),
    inf_sctr                 BOOLEAN      NOT NULL DEFAULT FALSE,
    inf_fecha_afiliacion     DATE         NOT NULL,
    inf_estado               VARCHAR(20)  NOT NULL DEFAULT 'ACTIVO',
    inf_documento_referencia VARCHAR(50),
    inf_activo               BOOLEAN      NOT NULL DEFAULT TRUE,
    inf_created_at           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    inf_updated_at           TIMESTAMP,
    CONSTRAINT fk_inf_trabajador FOREIGN KEY (inf_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_inf_afp FOREIGN KEY (inf_afp_id) REFERENCES tb_afps(afp_id),
    CONSTRAINT uq_inf_trabajador UNIQUE (inf_trabajador_id)
);

CREATE INDEX idx_inf_afp ON tb_informacion_pensionaria(inf_afp_id);
