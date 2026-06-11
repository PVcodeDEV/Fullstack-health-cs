-- =============================================================================
-- V19: Clinica — Cuenta / Cargos Adicionales
-- =============================================================================

-- tb_cargos_adicionales
CREATE TABLE tb_cargos_adicionales (
    cta_id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cta_cuenta_id       BIGINT        NOT NULL,
    cta_tipo            VARCHAR(30)   NOT NULL,
    cta_monto           DECIMAL(10,2) NOT NULL,
    cta_descripcion     VARCHAR(255),
    cta_fecha_registro  TIMESTAMP     NOT NULL,
    cta_usuario_id      BIGINT        NOT NULL,
    cta_created_at      TIMESTAMP     NOT NULL,
    cta_updated_at      TIMESTAMP,
    cta_activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cta_cuenta FOREIGN KEY (cta_cuenta_id)
        REFERENCES tb_cuentas(cue_id),
    CONSTRAINT fk_cta_usuario FOREIGN KEY (cta_usuario_id)
        REFERENCES tb_usuarios(usu_id),
    CONSTRAINT ck_cta_tipo CHECK (cta_tipo IN (
        'DIAS_EXTRA', 'CAMBIO_HABITACION', 'INSUMOS', 'OTROS'
    )),
    CONSTRAINT ck_cta_monto CHECK (cta_monto > 0)
);

-- Indexes
CREATE INDEX idx_cta_cuenta ON tb_cargos_adicionales(cta_cuenta_id);
CREATE INDEX idx_cta_tipo ON tb_cargos_adicionales(cta_tipo);
