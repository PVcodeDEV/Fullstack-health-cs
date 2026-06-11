-- =============================================================================
-- V20: RRHH Base — Foundation for HR Module
--
-- Steps IN ORDER:
--   1. CREATE tb_tipos_contrato + seed (6 rows)
--   2. CREATE tb_tipos_colegiatura + seed (4 rows)
--   3. ALTER tb_trabajadores — ADD COLUMN (12 columns)
--   4. CMP backfill from tb_medicos
--   5. CREATE tb_periodos_laborales + backfill
--   6. CREATE tb_contratos + indexes
-- =============================================================================

-- ============================================
-- STEP 1: Catalog tables in maestro
-- ============================================

CREATE TABLE tb_tipos_contrato (
    tco_id          BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    tco_codigo      VARCHAR(20)  NOT NULL UNIQUE,
    tco_nombre      VARCHAR(100) NOT NULL,
    tco_descripcion VARCHAR(255),
    tco_activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    tco_created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tco_updated_at  TIMESTAMP
);

-- Seed tipos contrato
INSERT INTO tb_tipos_contrato (tco_codigo, tco_nombre, tco_descripcion) VALUES
    ('INDETERMINADO', 'Contrato Indeterminado', 'Contrato a plazo no determinado - REMYPE'),
    ('DETERMINADO', 'Contrato a Plazo Determinado', 'Contrato por tiempo específico'),
    ('CAS', 'Contrato Administrativo de Servicios', 'Régimen CAS'),
    ('LOCACION', 'Locación de Servicios', 'Locación de servicios - Recibo por honorarios'),
    ('TIEMPO_PARCIAL', 'Tiempo Parcial', 'Jornada menor a 4 horas diarias'),
    ('INTERMITENTE', 'Intermitente', 'Servicios intermitentes');

-- ============================================
-- STEP 2: Catalog tables in maestro
-- ============================================

CREATE TABLE tb_tipos_colegiatura (
    tcl_id          BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    tcl_codigo      VARCHAR(20)  NOT NULL UNIQUE,
    tcl_nombre      VARCHAR(100) NOT NULL,
    tcl_descripcion VARCHAR(255),
    tcl_activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    tcl_created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    tcl_updated_at  TIMESTAMP
);

-- Seed tipos colegiatura
INSERT INTO tb_tipos_colegiatura (tcl_codigo, tcl_nombre) VALUES
    ('CMP', 'Colegio Médico del Perú'),
    ('CEP', 'Colegio de Enfermeros del Perú'),
    ('CPN', 'Colegio de Nutricionistas del Perú'),
    ('OTROS', 'Otros colegios profesionales');

-- ============================================
-- STEP 3: ALTER tb_trabajadores — add 12 columns
-- ============================================

ALTER TABLE tb_trabajadores
    ADD COLUMN tra_tipo              VARCHAR(30),
    ADD COLUMN tra_regimen_laboral   VARCHAR(20),
    ADD COLUMN tra_motivo_cese       VARCHAR(100),
    ADD COLUMN tra_banco             VARCHAR(60),
    ADD COLUMN tra_cuenta_sueldo     VARCHAR(20),
    ADD COLUMN tra_cci               VARCHAR(23),
    ADD COLUMN tra_contacto_nombre   VARCHAR(150),
    ADD COLUMN tra_contacto_telefono VARCHAR(15),
    ADD COLUMN tra_cantidad_hijos    INTEGER      NOT NULL DEFAULT 0,
    ADD COLUMN tra_colegiatura_numero  VARCHAR(20),
    ADD COLUMN tra_colegiatura_tipo_id BIGINT      REFERENCES tb_tipos_colegiatura(tcl_id),
    ADD COLUMN tra_discapacidad      BOOLEAN      NOT NULL DEFAULT FALSE,
    ADD COLUMN tra_sindicalizado     BOOLEAN      NOT NULL DEFAULT FALSE;

-- Let fecha_ingreso be NULL, handled by periods
ALTER TABLE tb_trabajadores
    ALTER COLUMN tra_fecha_ingreso DROP NOT NULL;

-- ============================================
-- STEP 4: CMP backfill from Medico
-- ============================================

UPDATE tb_trabajadores t
SET
    tra_colegiatura_numero = m.med_cmp,
    tra_colegiatura_tipo_id = (SELECT tcl_id FROM tb_tipos_colegiatura WHERE tcl_codigo = 'CMP'),
    tra_tipo = 'MEDICO'
FROM tb_medicos m
WHERE t.tra_id = m.med_trabajador_id
  AND m.med_cmp IS NOT NULL;

-- ============================================
-- STEP 5: Periodos Laborales
-- ============================================

CREATE TABLE tb_periodos_laborales (
    pla_id              BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    pla_trabajador_id   BIGINT       NOT NULL,
    pla_fecha_inicio    DATE         NOT NULL,
    pla_fecha_cese      DATE,
    pla_motivo_cese     VARCHAR(100),
    pla_es_reingreso    BOOLEAN      NOT NULL DEFAULT FALSE,
    pla_activo          BOOLEAN      NOT NULL DEFAULT TRUE,
    pla_created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    pla_updated_at      TIMESTAMP,
    CONSTRAINT fk_pla_trabajador FOREIGN KEY (pla_trabajador_id) REFERENCES tb_trabajadores(tra_id)
);

CREATE UNIQUE INDEX idx_pla_unique_active ON tb_periodos_laborales(pla_trabajador_id);

-- Backfill existing trabajadores with initial period
INSERT INTO tb_periodos_laborales (pla_trabajador_id, pla_fecha_inicio, pla_activo)
    SELECT tra_id, COALESCE(tra_fecha_ingreso, CURRENT_DATE), TRUE
    FROM tb_trabajadores
    WHERE NOT EXISTS (
        SELECT 1 FROM tb_periodos_laborales WHERE pla_trabajador_id = tb_trabajadores.tra_id AND pla_activo = TRUE
    );

-- ============================================
-- STEP 6: Contratos
-- ============================================

CREATE TABLE tb_contratos (
    con_id                  BIGINT GENERATED ALWAYS AS IDENTITY      PRIMARY KEY,
    con_trabajador_id       BIGINT         NOT NULL,
    con_tipo_contrato_id    BIGINT         NOT NULL,
    con_fecha_inicio        DATE           NOT NULL,
    con_fecha_fin           DATE,
    con_periodo_prueba_meses INTEGER,
    con_remuneracion        NUMERIC(10,2)  NOT NULL,
    con_jornada             VARCHAR(20)    NOT NULL DEFAULT 'REGULAR',
    con_estado              VARCHAR(20)    NOT NULL DEFAULT 'ACTIVO',
    con_documento_id        BIGINT,
    con_activo              BOOLEAN        NOT NULL DEFAULT TRUE,
    con_created_at          TIMESTAMP      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    con_updated_at          TIMESTAMP,
    CONSTRAINT fk_con_trabajador FOREIGN KEY (con_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_con_tipo_contrato FOREIGN KEY (con_tipo_contrato_id) REFERENCES tb_tipos_contrato(tco_id),
    CONSTRAINT ck_con_estado CHECK (con_estado IN ('ACTIVO', 'SUSPENDIDO', 'VENCIDO', 'RESUELTO'))
);

CREATE INDEX idx_con_trabajador ON tb_contratos(con_trabajador_id);
CREATE INDEX idx_con_estado ON tb_contratos(con_estado);
