-- =============================================================================
-- V15: Clinica — Admisión (Cuentas, Solicitudes, Diagnósticos)
-- =============================================================================

-- tb_cuentas
CREATE TABLE tb_cuentas (
    cue_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cue_paciente_id         BIGINT        NOT NULL,
    cue_paquete_quirurgico_id BIGINT,
    cue_tipo_habitacion_id  BIGINT,
    cue_fecha_apertura      TIMESTAMP     NOT NULL,
    cue_estado              VARCHAR(20)   NOT NULL DEFAULT 'ABIERTO',
    cue_total_estimado      DECIMAL(10,2),
    cue_total_cargos        DECIMAL(10,2) NOT NULL DEFAULT 0.00,
    cue_pendiente_cobro     BOOLEAN       NOT NULL DEFAULT FALSE,
    cue_created_at          TIMESTAMP     NOT NULL,
    cue_updated_at          TIMESTAMP,
    cue_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cue_paciente FOREIGN KEY (cue_paciente_id)
        REFERENCES tb_pacientes(pac_id),
    CONSTRAINT fk_cue_tipo_habitacion FOREIGN KEY (cue_tipo_habitacion_id)
        REFERENCES tb_tipos_habitacion(thab_id),
    CONSTRAINT ck_cue_estado CHECK (cue_estado IN ('ABIERTO', 'PENDIENTE_COBRO', 'CERRADO'))
);

-- tb_cuentas_paquetes
CREATE TABLE tb_cuentas_paquetes (
    cup_id                      BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cup_cuenta_id               BIGINT      NOT NULL,
    cup_paquete_quirurgico_id   BIGINT      NOT NULL,
    cup_fecha_registro          TIMESTAMP   NOT NULL,
    cup_created_at              TIMESTAMP   NOT NULL,
    cup_updated_at              TIMESTAMP,
    cup_activo                  BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cup_cuenta FOREIGN KEY (cup_cuenta_id)
        REFERENCES tb_cuentas(cue_id)
);

-- tb_solicitudes_hospitalizacion
CREATE TABLE tb_solicitudes_hospitalizacion (
    sol_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sol_cuenta_id           BIGINT        NOT NULL,
    sol_tipo_habitacion_id  BIGINT        NOT NULL,
    sol_cama_id             BIGINT,
    sol_estado              VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    sol_fecha_solicitud     TIMESTAMP     NOT NULL,
    sol_fecha_ingreso       TIMESTAMP,
    sol_created_at          TIMESTAMP     NOT NULL,
    sol_updated_at          TIMESTAMP,
    sol_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_sol_cuenta FOREIGN KEY (sol_cuenta_id)
        REFERENCES tb_cuentas(cue_id),
    CONSTRAINT fk_sol_tipo_habitacion FOREIGN KEY (sol_tipo_habitacion_id)
        REFERENCES tb_tipos_habitacion(thab_id),
    CONSTRAINT fk_sol_cama FOREIGN KEY (sol_cama_id)
        REFERENCES tb_camas(cama_id),
    CONSTRAINT ck_sol_estado CHECK (sol_estado IN (
        'PENDIENTE', 'CONFIRMADA', 'HOSPITALIZADO', 'ALTA_CLINICA', 'FINALIZADO'
    ))
);

-- tb_admision_diagnosticos
CREATE TABLE tb_admision_diagnosticos (
    diag_id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    diag_cuenta_id       BIGINT        NOT NULL,
    diag_codigo_cie11    VARCHAR(8)    NOT NULL,
    diag_tipo            VARCHAR(15)   NOT NULL DEFAULT 'PRINCIPAL',
    diag_created_at      TIMESTAMP     NOT NULL,
    diag_updated_at      TIMESTAMP,
    diag_activo          BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_diag_cuenta FOREIGN KEY (diag_cuenta_id)
        REFERENCES tb_cuentas(cue_id),
    CONSTRAINT fk_diag_cie11 FOREIGN KEY (diag_codigo_cie11)
        REFERENCES tb_cie11_diagnosticos(cie_codigo),
    CONSTRAINT ck_diag_tipo CHECK (diag_tipo IN ('PRINCIPAL', 'SECUNDARIO'))
);

-- Indexes
CREATE INDEX idx_cue_paciente ON tb_cuentas(cue_paciente_id);
CREATE INDEX idx_cue_estado ON tb_cuentas(cue_estado);
CREATE INDEX idx_cup_cuenta ON tb_cuentas_paquetes(cup_cuenta_id);
CREATE INDEX idx_sol_cuenta ON tb_solicitudes_hospitalizacion(sol_cuenta_id);
CREATE INDEX idx_sol_estado ON tb_solicitudes_hospitalizacion(sol_estado);
CREATE INDEX idx_sol_cama ON tb_solicitudes_hospitalizacion(sol_cama_id);
CREATE INDEX idx_diag_cuenta ON tb_admision_diagnosticos(diag_cuenta_id);
CREATE INDEX idx_diag_cie11 ON tb_admision_diagnosticos(diag_codigo_cie11);
