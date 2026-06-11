-- =============================================================================
-- V16: Clinica — Hospitalización
-- =============================================================================

-- tb_hospitalizaciones
CREATE TABLE tb_hospitalizaciones (
    hosp_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hosp_solicitud_id       BIGINT        NOT NULL UNIQUE,
    hosp_cuenta_id          BIGINT        NOT NULL,
    hosp_paciente_id        BIGINT        NOT NULL,
    hosp_cama_id            BIGINT        NOT NULL,
    hosp_fecha_ingreso      TIMESTAMP     NOT NULL,
    hosp_fecha_alta         TIMESTAMP,
    hosp_estado             VARCHAR(20)   NOT NULL DEFAULT 'HOSPITALIZADO',
    hosp_tiene_reporte_operatorio BOOLEAN NOT NULL DEFAULT FALSE,
    hosp_created_at         TIMESTAMP     NOT NULL,
    hosp_updated_at         TIMESTAMP,
    hosp_activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_hosp_solicitud FOREIGN KEY (hosp_solicitud_id)
        REFERENCES tb_solicitudes_hospitalizacion(sol_id),
    CONSTRAINT fk_hosp_cuenta FOREIGN KEY (hosp_cuenta_id)
        REFERENCES tb_cuentas(cue_id),
    CONSTRAINT fk_hosp_paciente FOREIGN KEY (hosp_paciente_id)
        REFERENCES tb_pacientes(pac_id),
    CONSTRAINT fk_hosp_cama FOREIGN KEY (hosp_cama_id)
        REFERENCES tb_camas(cama_id),
    CONSTRAINT ck_hosp_estado CHECK (hosp_estado IN (
        'HOSPITALIZADO', 'ALTA_CLINICA', 'FINALIZADO'
    ))
);

-- tb_cambios_habitacion
CREATE TABLE tb_cambios_habitacion (
    cam_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cam_hospitalizacion_id BIGINT  NOT NULL,
    cam_cama_origen_id     BIGINT  NOT NULL,
    cam_cama_destino_id    BIGINT  NOT NULL,
    cam_usuario_id         BIGINT  NOT NULL,
    cam_fecha_cambio       TIMESTAMP NOT NULL,
    cam_motivo             VARCHAR(255),
    cam_created_at         TIMESTAMP NOT NULL,
    cam_updated_at         TIMESTAMP,
    cam_activo             BOOLEAN   NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cam_hospitalizacion FOREIGN KEY (cam_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_cam_cama_origen FOREIGN KEY (cam_cama_origen_id)
        REFERENCES tb_camas(cama_id),
    CONSTRAINT fk_cam_cama_destino FOREIGN KEY (cam_cama_destino_id)
        REFERENCES tb_camas(cama_id),
    CONSTRAINT fk_cam_usuario FOREIGN KEY (cam_usuario_id)
        REFERENCES tb_usuarios(usu_id)
);

-- tb_notas_evolucion
CREATE TABLE tb_notas_evolucion (
    nota_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    nota_hospitalizacion_id  BIGINT        NOT NULL,
    nota_fecha_hora          TIMESTAMP     NOT NULL,
    nota_usuario_id          BIGINT        NOT NULL,
    nota_descripcion         TEXT          NOT NULL,
    nota_plan                TEXT,
    nota_tipo                VARCHAR(20)   NOT NULL DEFAULT 'EVOLUCION',
    nota_signos_vitales      TEXT,
    nota_created_at          TIMESTAMP     NOT NULL,
    nota_updated_at          TIMESTAMP,
    nota_activo              BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_nota_hospitalizacion FOREIGN KEY (nota_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_nota_usuario FOREIGN KEY (nota_usuario_id)
        REFERENCES tb_usuarios(usu_id),
    CONSTRAINT ck_nota_tipo CHECK (nota_tipo IN ('EVOLUCION', 'ENFERMERIA'))
);

-- tb_solicitudes_medicamento
CREATE TABLE tb_solicitudes_medicamento (
    smed_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    smed_hospitalizacion_id BIGINT        NOT NULL,
    smed_medicamento_id     BIGINT        NOT NULL,
    smed_dosis              VARCHAR(50)   NOT NULL,
    smed_frecuencia         VARCHAR(50)   NOT NULL,
    smed_via_administracion_id BIGINT,
    smed_fecha_inicio       DATE          NOT NULL,
    smed_fecha_fin          DATE,
    smed_estado             VARCHAR(20)   NOT NULL DEFAULT 'PENDIENTE',
    smed_usuario_id         BIGINT        NOT NULL,
    smed_created_at         TIMESTAMP     NOT NULL,
    smed_updated_at         TIMESTAMP,
    smed_activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_smed_hospitalizacion FOREIGN KEY (smed_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_smed_usuario FOREIGN KEY (smed_usuario_id)
        REFERENCES tb_usuarios(usu_id),
    CONSTRAINT ck_smed_estado CHECK (smed_estado IN ('PENDIENTE', 'ATENDIDA', 'CANCELADA'))
);

-- tb_altas_medicas
CREATE TABLE tb_altas_medicas (
    alt_id                 BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alt_hospitalizacion_id BIGINT        NOT NULL UNIQUE,
    alt_fecha_alta         TIMESTAMP     NOT NULL,
    alt_tipo_alta          VARCHAR(20)   NOT NULL,
    alt_diagnostico_final  TEXT          NOT NULL,
    alt_medico_id          BIGINT        NOT NULL,
    alt_observaciones      TEXT,
    alt_created_at         TIMESTAMP     NOT NULL,
    alt_updated_at         TIMESTAMP,
    alt_activo             BOOLEAN       NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_alt_hospitalizacion FOREIGN KEY (alt_hospitalizacion_id)
        REFERENCES tb_hospitalizaciones(hosp_id),
    CONSTRAINT fk_alt_medico FOREIGN KEY (alt_medico_id)
        REFERENCES tb_medicos(med_id),
    CONSTRAINT ck_alt_tipo CHECK (alt_tipo_alta IN (
        'MEJORADO', 'VOLUNTARIO', 'TRASLADO', 'FALLECIDO'
    ))
);

-- Indexes
CREATE INDEX idx_hosp_solicitud ON tb_hospitalizaciones(hosp_solicitud_id);
CREATE INDEX idx_hosp_cuenta ON tb_hospitalizaciones(hosp_cuenta_id);
CREATE INDEX idx_hosp_paciente ON tb_hospitalizaciones(hosp_paciente_id);
CREATE INDEX idx_hosp_cama ON tb_hospitalizaciones(hosp_cama_id);
CREATE INDEX idx_hosp_estado ON tb_hospitalizaciones(hosp_estado);
CREATE INDEX idx_cam_hospitalizacion ON tb_cambios_habitacion(cam_hospitalizacion_id);
CREATE INDEX idx_nota_hospitalizacion ON tb_notas_evolucion(nota_hospitalizacion_id);
CREATE INDEX idx_nota_tipo ON tb_notas_evolucion(nota_tipo);
CREATE INDEX idx_smed_hospitalizacion ON tb_solicitudes_medicamento(smed_hospitalizacion_id);
CREATE INDEX idx_smed_estado ON tb_solicitudes_medicamento(smed_estado);
CREATE INDEX idx_alt_hospitalizacion ON tb_altas_medicas(alt_hospitalizacion_id);
