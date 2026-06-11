-- ============================================================
-- V33: Farmacia - Venta + DetalleVenta + SesionCaja stub
-- ============================================================

-- ============================================================
-- Table: tb_sesiones_caja (MINIMAL STUB — extended in V34)
-- Prefix: scaj_
-- ============================================================
CREATE TABLE tb_sesiones_caja (
    scaj_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    scaj_usuario_id         BIGINT NOT NULL,
    scaj_estado             VARCHAR(16) NOT NULL DEFAULT 'ABIERTA'
                            CHECK (scaj_estado IN ('ABIERTA', 'CERRADA')),
    scaj_created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scaj_updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    scaj_activo             BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE INDEX idx_scaj_estado ON tb_sesiones_caja(scaj_estado);
CREATE INDEX idx_scaj_usuario ON tb_sesiones_caja(scaj_usuario_id);

-- ============================================================
-- Table: tb_ventas
-- Prefix: vent_
-- ============================================================
CREATE TABLE tb_ventas (
    vent_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    vent_sesion_caja_id     BIGINT NOT NULL,
    vent_correlativo        INTEGER NOT NULL,
    vent_cliente_persona_id BIGINT,
    vent_tipo_lista         VARCHAR(16) NOT NULL DEFAULT 'PUBLICO'
                            CHECK (vent_tipo_lista IN ('MEDICO', 'PUBLICO')),
    vent_subtotal           NUMERIC(12,2) NOT NULL,
    vent_descuento_total    NUMERIC(12,2) NOT NULL DEFAULT 0,
    vent_impuesto           NUMERIC(12,2) NOT NULL,
    vent_total              NUMERIC(12,2) NOT NULL,
    vent_estado             VARCHAR(16) NOT NULL DEFAULT 'COMPLETADA'
                            CHECK (vent_estado IN ('COMPLETADA', 'ANULADA')),
    vent_vendedor_usuario_id BIGINT NOT NULL,
    vent_observaciones      VARCHAR(500),
    vent_created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vent_updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    vent_activo             BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT fk_vent_sesion FOREIGN KEY (vent_sesion_caja_id)
        REFERENCES tb_sesiones_caja(scaj_id),
    CONSTRAINT fk_vent_cliente FOREIGN KEY (vent_cliente_persona_id)
        REFERENCES tb_personas(person_id),
    CONSTRAINT uq_vent_sesion_correlativo UNIQUE (vent_sesion_caja_id, vent_correlativo)
);

CREATE INDEX idx_vent_sesion ON tb_ventas(vent_sesion_caja_id);
CREATE INDEX idx_vent_cliente ON tb_ventas(vent_cliente_persona_id);
CREATE INDEX idx_vent_estado ON tb_ventas(vent_estado);
CREATE INDEX idx_vent_created ON tb_ventas(vent_created_at DESC);

-- ============================================================
-- Table: tb_detalle_ventas
-- Prefix: dvt_
-- ============================================================
CREATE TABLE tb_detalle_ventas (
    dvt_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    dvt_venta_id            BIGINT NOT NULL,
    dvt_lote_id             BIGINT NOT NULL,
    dvt_cantidad            INTEGER NOT NULL CHECK (dvt_cantidad > 0),
    dvt_precio_unitario     NUMERIC(12,4) NOT NULL,
    dvt_precio_original     NUMERIC(12,4) NOT NULL,
    dvt_descuento_aplicado  NUMERIC(12,4) NOT NULL DEFAULT 0,
    dvt_subtotal            NUMERIC(12,2) NOT NULL,
    dvt_created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_dvt_venta FOREIGN KEY (dvt_venta_id)
        REFERENCES tb_ventas(vent_id) ON DELETE CASCADE,
    CONSTRAINT fk_dvt_lote FOREIGN KEY (dvt_lote_id)
        REFERENCES tb_lotes(lote_id)
);

CREATE INDEX idx_dvt_venta ON tb_detalle_ventas(dvt_venta_id);
CREATE INDEX idx_dvt_lote ON tb_detalle_ventas(dvt_lote_id);
