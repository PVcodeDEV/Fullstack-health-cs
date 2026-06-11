-- ============================================================
-- V32: Farmacia - Lote + MovimientoStock + Config seeding
-- ============================================================

-- ============================================================
-- Table: tb_lotes
-- Prefix: lote_
-- ============================================================
CREATE TABLE tb_lotes (
    lote_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    lote_producto_id        BIGINT NOT NULL,
    lote_codigo_lote        VARCHAR(100) NOT NULL,
    lote_fecha_vencimiento  DATE NOT NULL,
    lote_stock_inicial      INTEGER NOT NULL,
    lote_stock_actual       INTEGER NOT NULL,
    lote_precio_costo       DECIMAL(12,4) NOT NULL,
    lote_almacen_id         BIGINT NOT NULL,
    lote_created_at         TIMESTAMP NOT NULL,
    lote_updated_at         TIMESTAMP,
    lote_activo             BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_lote_producto FOREIGN KEY (lote_producto_id)
        REFERENCES tb_productos(prod_id),
    CONSTRAINT fk_lote_almacen FOREIGN KEY (lote_almacen_id)
        REFERENCES tb_almacenes(alm_id),
    CONSTRAINT ck_lote_stock CHECK (lote_stock_actual >= 0)
);

CREATE INDEX idx_lote_producto ON tb_lotes(lote_producto_id);
CREATE INDEX idx_lote_almacen ON tb_lotes(lote_almacen_id);
CREATE INDEX idx_lote_vencimiento ON tb_lotes(lote_fecha_vencimiento, lote_producto_id);
CREATE INDEX idx_lote_stock ON tb_lotes(lote_producto_id, lote_stock_actual);

-- ============================================================
-- Table: tb_movimientos_stock
-- Prefix: movs_
-- ============================================================
CREATE TABLE tb_movimientos_stock (
    movs_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    movs_lote_id            BIGINT NOT NULL,
    movs_tipo               VARCHAR(20) NOT NULL CHECK (movs_tipo IN ('ENTRADA', 'SALIDA', 'AJUSTE', 'TRANSFERENCIA')),
    movs_cantidad           INTEGER NOT NULL,
    movs_motivo             VARCHAR(255),
    movs_venta_id           BIGINT,
    movs_usuario_id         BIGINT,
    movs_almacen_origen_id  BIGINT,
    movs_almacen_destino_id BIGINT,
    movs_created_at         TIMESTAMP NOT NULL,
    movs_updated_at         TIMESTAMP,
    movs_activo             BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_movs_lote FOREIGN KEY (movs_lote_id)
        REFERENCES tb_lotes(lote_id),
    -- NOTE: FK to tb_ventas(vent_id) intentionally omitted here because
    -- tb_ventas is created in V33 (which runs after this migration).
    -- The FK will be added in a later migration for H2 compatibility.
    CONSTRAINT fk_movs_usuario FOREIGN KEY (movs_usuario_id)
        REFERENCES tb_usuarios(usu_id),
    CONSTRAINT fk_movs_almacen_origen FOREIGN KEY (movs_almacen_origen_id)
        REFERENCES tb_almacenes(alm_id),
    CONSTRAINT fk_movs_almacen_destino FOREIGN KEY (movs_almacen_destino_id)
        REFERENCES tb_almacenes(alm_id)
);

CREATE INDEX idx_movs_lote ON tb_movimientos_stock(movs_lote_id);
CREATE INDEX idx_movs_tipo ON tb_movimientos_stock(movs_tipo);
CREATE INDEX idx_movs_venta ON tb_movimientos_stock(movs_venta_id);

-- ============================================================
-- Config seeding: farmacia defaults in tb_configuracion_api
-- PRE-04: Pricing + Discount thresholds
-- ============================================================
MERGE INTO tb_configuracion_api AS target
USING (VALUES
    ('farmacia', 'umbral_costo',               '90',  'decimal', true, NOW()),
    ('farmacia', 'utilidad_base',              '20',  'decimal', true, NOW()),
    ('farmacia', 'utilidad_alta_min',          '10',  'decimal', true, NOW()),
    ('farmacia', 'utilidad_alta_max',          '20',  'decimal', true, NOW()),
    ('farmacia', 'descuento_vencimiento_dias', '90',  'integer', true, NOW()),
    ('farmacia', 'descuento_vencimiento_max_pct', '20', 'decimal', true, NOW()),
    ('farmacia', 'igv',                        '18',  'decimal', true, NOW())
) AS src(modulo, clave, valor, tipo, activo, created_at)
ON target.conf_modulo = src.modulo AND target.conf_clave = src.clave
WHEN NOT MATCHED THEN
    INSERT (conf_modulo, conf_clave, conf_valor, conf_tipo, conf_activo, conf_created_at)
    VALUES (src.modulo, src.clave, src.valor, src.tipo, src.activo, src.created_at);
