-- =============================================================================
-- V43: Crear tb_numeracion_control y tb_tipos_movimiento
-- Seed data for numeración, tipos de movimiento
-- Fix V32 CHECK constraint: add DEVOLUCION to allowed movement types
-- =============================================================================

-- =============================================================================
-- Fix V32: Add DEVOLUCION to the CHECK constraint on tb_movimientos_stock
-- PostgreSQL auto-names inline CHECK constraints as tablename_column_check
-- =============================================================================
ALTER TABLE tb_movimientos_stock DROP CONSTRAINT IF EXISTS tb_movimientos_stock_movs_tipo_check;
ALTER TABLE tb_movimientos_stock ADD CONSTRAINT ck_movs_tipo
    CHECK (movs_tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'TRANSFERENCIA', 'DEVOLUCION'));

-- =============================================================================
-- Table: tb_numeracion_control
-- Prefix: numc_
-- Unique: (entidad, serie, anio)
-- =============================================================================
CREATE TABLE tb_numeracion_control (
    numc_id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    numc_entidad                 VARCHAR(50) NOT NULL,
    numc_serie                   VARCHAR(10) NOT NULL,
    numc_correlativo_actual      BIGINT NOT NULL DEFAULT 0,
    numc_prefijo                 VARCHAR(10),
    numc_longitud_correlativo    INTEGER NOT NULL DEFAULT 6,
    numc_anio                    INTEGER NOT NULL,
    numc_created_at              TIMESTAMP NOT NULL,
    numc_updated_at              TIMESTAMP,
    numc_activo                  BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_numc_entidad_serie_anio UNIQUE (numc_entidad, numc_serie, numc_anio)
);

CREATE INDEX idx_numc_entidad ON tb_numeracion_control(numc_entidad);
CREATE INDEX idx_numc_entidad_anio ON tb_numeracion_control(numc_entidad, numc_anio);

-- =============================================================================
-- Table: tb_tipos_movimiento
-- Prefix: tim_
-- Unique: codigo
-- =============================================================================
CREATE TABLE tb_tipos_movimiento (
    tim_id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tim_codigo                   VARCHAR(30) NOT NULL,
    tim_nombre                   VARCHAR(100) NOT NULL,
    tim_modulo                   VARCHAR(50) NOT NULL,
    tim_descripcion              VARCHAR(255),
    tim_created_at               TIMESTAMP NOT NULL,
    tim_updated_at               TIMESTAMP,
    tim_activo                   BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_tim_codigo UNIQUE (tim_codigo)
);

CREATE INDEX idx_tim_modulo ON tb_tipos_movimiento(tim_modulo);
CREATE INDEX idx_tim_codigo ON tb_tipos_movimiento(tim_codigo);

-- =============================================================================
-- Seed: 5 tipos de movimiento for FARMACIA
-- =============================================================================
MERGE INTO tb_tipos_movimiento AS target
USING (VALUES
    ('ENTRADA',       'Entrada',        'FARMACIA', 'Entrada de productos al almacén',           true, NOW()),
    ('SALIDA',        'Salida',         'FARMACIA', 'Salida de productos del almacén',           true, NOW()),
    ('AJUSTE',        'Ajuste',         'FARMACIA', 'Ajuste de inventario por diferencias',      true, NOW()),
    ('TRANSFERENCIA', 'Transferencia',  'FARMACIA', 'Transferencia entre almacenes',             true, NOW()),
    ('DEVOLUCION',    'Devolución',     'FARMACIA', 'Devolución de productos',                   true, NOW())
) AS src(codigo, nombre, modulo, descripcion, activo, created_at)
ON target.tim_codigo = src.codigo
WHEN NOT MATCHED THEN
    INSERT (tim_codigo, tim_nombre, tim_modulo, tim_descripcion, tim_activo, tim_created_at)
    VALUES (src.codigo, src.nombre, src.modulo, src.descripcion, src.activo, src.created_at);

-- =============================================================================
-- Seed: 3 numeración entries (COMPROBANTE, VENTA, HC) for current year
-- =============================================================================
MERGE INTO tb_numeracion_control AS target
USING (VALUES
    ('COMPROBANTE', '001', 0, NULL,     6, EXTRACT(YEAR FROM CURRENT_DATE), true, NOW()),
    ('VENTA',       '001', 0, NULL,     6, EXTRACT(YEAR FROM CURRENT_DATE), true, NOW()),
    ('HC',          '001', 0, 'HC-',    6, EXTRACT(YEAR FROM CURRENT_DATE), true, NOW())
) AS src(entidad, serie, correlativo_actual, prefijo, longitud, anio, activo, created_at)
ON target.numc_entidad = src.entidad AND target.numc_serie = src.serie AND target.numc_anio = src.anio
WHEN NOT MATCHED THEN
    INSERT (numc_entidad, numc_serie, numc_correlativo_actual, numc_prefijo, numc_longitud_correlativo, numc_anio, numc_activo, numc_created_at)
    VALUES (src.entidad, src.serie, src.correlativo_actual, src.prefijo, src.longitud, src.anio, src.activo, src.created_at);
