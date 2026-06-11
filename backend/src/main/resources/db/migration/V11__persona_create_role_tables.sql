-- =============================================================================
-- V11: Create role tables for Paciente, Trabajador, and Medico
-- =============================================================================

-- Paciente (clinical module) — FK to tb_personas
CREATE TABLE tb_pacientes (
    pac_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pac_persona_id BIGINT NOT NULL UNIQUE,
    pac_tipo_paciente VARCHAR(20) NOT NULL DEFAULT 'PARTICULAR',
    pac_nro_historia_clinica VARCHAR(20) UNIQUE,
    pac_grupo_sanguineo VARCHAR(5),
    pac_alergias TEXT,
    pac_contacto_emergencia_nombre VARCHAR(200),
    pac_contacto_emergencia_telefono VARCHAR(20),
    pac_created_at TIMESTAMP NOT NULL,
    pac_updated_at TIMESTAMP,
    pac_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_pac_persona FOREIGN KEY (pac_persona_id) REFERENCES tb_personas(pers_persona_id)
);

-- Trabajador (HR module) — FK to tb_personas
CREATE TABLE tb_trabajadores (
    tra_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tra_persona_id BIGINT NOT NULL UNIQUE,
    tra_codigo_trabajador VARCHAR(20) NOT NULL UNIQUE,
    tra_fecha_ingreso DATE,
    tra_cargo VARCHAR(200),
    tra_area_funcional_id BIGINT,
    tra_created_at TIMESTAMP NOT NULL,
    tra_updated_at TIMESTAMP,
    tra_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_tra_persona FOREIGN KEY (tra_persona_id) REFERENCES tb_personas(pers_persona_id),
    CONSTRAINT fk_tra_area_funcional FOREIGN KEY (tra_area_funcional_id) REFERENCES tb_areas_funcionales(areaf_id)
);

-- Medico (clinical module) — FK to tb_trabajadores
CREATE TABLE tb_medicos (
    med_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    med_trabajador_id BIGINT NOT NULL UNIQUE,
    med_cmp VARCHAR(20) NOT NULL UNIQUE,
    med_especialidad_id BIGINT,
    med_es_especialista BOOLEAN DEFAULT FALSE,
    med_created_at TIMESTAMP NOT NULL,
    med_updated_at TIMESTAMP,
    med_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_med_trabajador FOREIGN KEY (med_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_med_especialidad FOREIGN KEY (med_especialidad_id) REFERENCES tb_especialidades_medicas(espm_id)
);

-- Indexes for performance
CREATE INDEX idx_pac_tipo_paciente ON tb_pacientes(pac_tipo_paciente);
CREATE INDEX idx_tra_area_funcional ON tb_trabajadores(tra_area_funcional_id);
CREATE INDEX idx_med_especialidad ON tb_medicos(med_especialidad_id);
CREATE INDEX idx_pac_activo ON tb_pacientes(pac_activo);
CREATE INDEX idx_tra_activo ON tb_trabajadores(tra_activo);
CREATE INDEX idx_med_activo ON tb_medicos(med_activo);
