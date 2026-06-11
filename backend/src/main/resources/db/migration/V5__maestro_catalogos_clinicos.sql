-- Clinical Catalogs: 6 tables + seed data
-- CIE-11 table is in V4__maestro_cie11.sql (separate migration, seed goes in V9)

-- ============================================================
-- Table: tb_especialidades_medicas
-- ============================================================
CREATE TABLE tb_especialidades_medicas (
    espm_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    espm_codigo         VARCHAR(10) NOT NULL UNIQUE,
    espm_nombre         VARCHAR(100) NOT NULL,
    espm_abreviatura    VARCHAR(10),
    espm_created_at     TIMESTAMP NOT NULL,
    espm_updated_at     TIMESTAMP,
    espm_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_tipos_paciente
-- ============================================================
CREATE TABLE tb_tipos_paciente (
    tpac_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tpac_codigo         VARCHAR(10) NOT NULL UNIQUE,
    tpac_nombre         VARCHAR(100) NOT NULL,
    tpac_created_at     TIMESTAMP NOT NULL,
    tpac_updated_at     TIMESTAMP,
    tpac_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_tipos_atencion
-- ============================================================
CREATE TABLE tb_tipos_atencion (
    tate_id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tate_codigo                 VARCHAR(10) NOT NULL UNIQUE,
    tate_nombre                 VARCHAR(100) NOT NULL,
    tate_requiere_habitacion    BOOLEAN NOT NULL DEFAULT FALSE,
    tate_created_at             TIMESTAMP NOT NULL,
    tate_updated_at             TIMESTAMP,
    tate_activo                 BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_vias_administracion
-- ============================================================
CREATE TABLE tb_vias_administracion (
    via_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    via_codigo         VARCHAR(10) NOT NULL UNIQUE,
    via_nombre         VARCHAR(100) NOT NULL,
    via_created_at     TIMESTAMP NOT NULL,
    via_updated_at     TIMESTAMP,
    via_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_formas_farmaceuticas
-- ============================================================
CREATE TABLE tb_formas_farmaceuticas (
    ffar_id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    ffar_codigo                 VARCHAR(10) NOT NULL UNIQUE,
    ffar_nombre                 VARCHAR(100) NOT NULL,
    ffar_requiere_preparacion   BOOLEAN NOT NULL DEFAULT FALSE,
    ffar_created_at             TIMESTAMP NOT NULL,
    ffar_updated_at             TIMESTAMP,
    ffar_activo                 BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_tipos_habitacion
-- ============================================================
CREATE TABLE tb_tipos_habitacion (
    thab_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    thab_codigo         VARCHAR(10) NOT NULL UNIQUE,
    thab_nombre         VARCHAR(100) NOT NULL,
    thab_tarifa_base    DECIMAL(10,2) NOT NULL,
    thab_capacidad      INT NOT NULL,
    thab_created_at     TIMESTAMP NOT NULL,
    thab_updated_at     TIMESTAMP,
    thab_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- EspecialidadMedica (15 rows)
INSERT INTO tb_especialidades_medicas (espm_codigo, espm_nombre, espm_abreviatura, espm_activo, espm_created_at) VALUES
    ('CG',  'Cirugía General',           'CG',  true, NOW()),
    ('TRA', 'Traumatología',             'TRA', true, NOW()),
    ('PED', 'Pediatría',                 'PED', true, NOW()),
    ('GIN', 'Ginecología',               'GIN', true, NOW()),
    ('ANE', 'Anestesiología',            'ANE', true, NOW()),
    ('MI',  'Medicina Interna',          'MI',  true, NOW()),
    ('CAR', 'Cardiología',               'CAR', true, NOW()),
    ('NEU', 'Neurología',                'NEU', true, NOW()),
    ('OFT', 'Oftalmología',              'OFT', true, NOW()),
    ('ORL', 'Otorrinolaringología',      'ORL', true, NOW()),
    ('URO', 'Urología',                  'URO', true, NOW()),
    ('DER', 'Dermatología',              'DER', true, NOW()),
    ('PSI', 'Psiquiatría',               'PSI', true, NOW()),
    ('RAD', 'Radiología',                'RAD', true, NOW()),
    ('EME', 'Medicina de Emergencia',    'EME', true, NOW());

-- TipoPaciente (4 rows)
INSERT INTO tb_tipos_paciente (tpac_codigo, tpac_nombre, tpac_activo, tpac_created_at) VALUES
    ('ASEG', 'Asegurado Essalud', true, NOW()),
    ('SIS',  'Afiliado SIS',      true, NOW()),
    ('PART', 'Particular',        true, NOW()),
    ('CONV', 'Convenio',          true, NOW());

-- TipoAtencion (4 rows)
INSERT INTO tb_tipos_atencion (tate_codigo, tate_nombre, tate_requiere_habitacion, tate_activo, tate_created_at) VALUES
    ('CEXT', 'Consulta Externa',     false, true, NOW()),
    ('EMER', 'Emergencia',           false, true, NOW()),
    ('HOSP', 'Hospitalización',      true,  true, NOW()),
    ('SOP',  'Sala de Operaciones',  false, true, NOW());

-- ViaAdministracion (9 rows)
INSERT INTO tb_vias_administracion (via_codigo, via_nombre, via_activo, via_created_at) VALUES
    ('ORAL', 'Oral',         true, NOW()),
    ('IV',   'Intravenosa',  true, NOW()),
    ('IM',   'Intramuscular', true, NOW()),
    ('SC',   'Subcutánea',   true, NOW()),
    ('TOP',  'Tópica',       true, NOW()),
    ('INH',  'Inhalatoria',  true, NOW()),
    ('REC',  'Rectal',       true, NOW()),
    ('OFT',  'Oftálmica',    true, NOW()),
    ('OTI',  'Ótica',        true, NOW());

-- FormaFarmaceutica (10 rows)
INSERT INTO tb_formas_farmaceuticas (ffar_codigo, ffar_nombre, ffar_requiere_preparacion, ffar_activo, ffar_created_at) VALUES
    ('TAB', 'Tableta',     false, true, NOW()),
    ('CAP', 'Cápsula',     false, true, NOW()),
    ('JBE', 'Jarabe',      false, true, NOW()),
    ('INY', 'Inyectable',  true,  true, NOW()),
    ('CRE', 'Crema',       false, true, NOW()),
    ('UNG', 'Ungüento',    false, true, NOW()),
    ('SUP', 'Supositorio', false, true, NOW()),
    ('GOT', 'Gotas',       false, true, NOW()),
    ('POL', 'Polvo',       false, true, NOW()),
    ('SOL', 'Solución',    false, true, NOW());

-- TipoHabitacion (4 rows)
INSERT INTO tb_tipos_habitacion (thab_codigo, thab_nombre, thab_tarifa_base, thab_capacidad, thab_activo, thab_created_at) VALUES
    ('IND', 'Individual',               250.00, 1, true, NOW()),
    ('COM2', 'Compartida 2 camas',      150.00, 2, true, NOW()),
    ('COM3', 'Compartida 3 camas',      100.00, 3, true, NOW()),
    ('SUIT', 'Suite',                   400.00, 1, true, NOW());
