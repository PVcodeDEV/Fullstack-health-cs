-- ============================================================
-- V34: Farmacia - SesionCaja (full) + Reposicion + Almacen CRUD
-- Compatible with PostgreSQL 18 and H2
-- ============================================================

-- ============================================================
-- ALTER tb_sesiones_caja — add new columns (V33 stub extended)
-- Prefix: scaj_
-- Preserves existing data and PK (scaj_id)
-- ============================================================

-- H2-compatible ALTER (ADD COLUMN IF NOT EXISTS would be PG-only;
-- since Flyway runs each migration once, add columns unconditionally).
-- For H2 compatibility, we use a simple ALTER TABLE.
-- In H2, ADD COLUMN IF NOT EXISTS is not supported, but the migration
-- runs exactly once so the safety check is only needed for PG replay safety.

ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_almacen_id              BIGINT NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_monto_apertura           NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_monto_cierre_esperado    NUMERIC(12,2) NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_monto_cierre_real        NUMERIC(12,2) NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_diferencia_cierre        NUMERIC(12,2) NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_total_ventas             NUMERIC(12,2) NOT NULL DEFAULT 0;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_fecha_apertura           TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_fecha_cierre             TIMESTAMP NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_observaciones_apertura   VARCHAR(500) NULL;
ALTER TABLE tb_sesiones_caja ADD COLUMN scaj_observaciones_cierre     VARCHAR(500) NULL;

-- FK to tb_almacenes (ON DELETE RESTRICT, nullable for back-compat)
ALTER TABLE tb_sesiones_caja
    ADD CONSTRAINT fk_scaj_almacen
        FOREIGN KEY (scaj_almacen_id)
        REFERENCES tb_almacenes(alm_id)
        ON DELETE RESTRICT;

-- Check constraint: monto_apertura >= 0
ALTER TABLE tb_sesiones_caja
    ADD CONSTRAINT chk_scaj_monto_apertura_no_negativo
        CHECK (scaj_monto_apertura >= 0);

-- New indexes
CREATE INDEX idx_scaj_usuario_estado ON tb_sesiones_caja(scaj_usuario_id, scaj_estado);
CREATE INDEX idx_scaj_fecha_apertura ON tb_sesiones_caja(scaj_fecha_apertura DESC);

-- ============================================================
-- Table: tb_reposicion (planning view, no FK to ventas)
-- Prefix: rep_
-- ============================================================
CREATE TABLE tb_reposicion (
    rep_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rep_generada_en         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    rep_usuario_id          BIGINT NOT NULL,
    rep_almacen_id          BIGINT NOT NULL,
    rep_observaciones       VARCHAR(500) NULL,
    rep_estado              VARCHAR(16) NOT NULL DEFAULT 'PENDIENTE'
                            CHECK (rep_estado IN ('PENDIENTE', 'PROCESADA', 'DESCARTADA')),
    rep_procesada_en        TIMESTAMP NULL,

    CONSTRAINT fk_rep_usuario FOREIGN KEY (rep_usuario_id)
        REFERENCES tb_usuarios(usu_id),
    CONSTRAINT fk_rep_almacen FOREIGN KEY (rep_almacen_id)
        REFERENCES tb_almacenes(alm_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_rep_estado     ON tb_reposicion(rep_estado);
CREATE INDEX idx_rep_almacen    ON tb_reposicion(rep_almacen_id);
CREATE INDEX idx_rep_generada   ON tb_reposicion(rep_generada_en DESC);

-- ============================================================
-- Table: tb_reposicion_detalle
-- Prefix: rdet_
-- ============================================================
CREATE TABLE tb_reposicion_detalle (
    rdet_id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rdet_reposicion_id          BIGINT NOT NULL,
    rdet_producto_id            BIGINT NOT NULL,
    rdet_stock_actual           INTEGER NOT NULL,
    rdet_stock_minimo           INTEGER NOT NULL,
    rdet_stock_critico          INTEGER NULL,
    rdet_cantidad_sugerida      INTEGER NOT NULL,
    rdet_proveedor_sugerido     VARCHAR(200) NULL,

    CONSTRAINT fk_rdet_reposicion FOREIGN KEY (rdet_reposicion_id)
        REFERENCES tb_reposicion(rep_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_rdet_producto FOREIGN KEY (rdet_producto_id)
        REFERENCES tb_productos(prod_id)
        ON DELETE RESTRICT
);

CREATE INDEX idx_rdet_reposicion   ON tb_reposicion_detalle(rdet_reposicion_id);
CREATE INDEX idx_rdet_producto     ON tb_reposicion_detalle(rdet_producto_id);
