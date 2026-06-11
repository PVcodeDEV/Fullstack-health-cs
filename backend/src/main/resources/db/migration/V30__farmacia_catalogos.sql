-- ============================================================
-- V30: Farmacia - Pharmacy catalog tables + seed data
-- ============================================================

-- ============================================================
-- Table: tb_tipos_medicamento
-- Prefix: tmed_
-- ============================================================
CREATE TABLE tb_tipos_medicamento (
    tmed_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tmed_codigo         VARCHAR(10) NOT NULL UNIQUE,
    tmed_nombre         VARCHAR(100) NOT NULL,
    tmed_created_at     TIMESTAMP NOT NULL,
    tmed_updated_at     TIMESTAMP,
    tmed_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_tipos_medicamento (tmed_codigo, tmed_nombre, tmed_activo, tmed_created_at) VALUES
    ('MARCA',     'Medicamento de Marca',          true, NOW()),
    ('GENERICO',  'Medicamento Genérico',          true, NOW()),
    ('BIOLOGICO', 'Producto Biológico',            true, NOW()),
    ('CONTROL',   'Medicamento Controlado',        true, NOW()),
    ('FITOTER',   'Fitoterapéutico',               true, NOW());

-- ============================================================
-- Table: tb_formas_presentacion
-- Prefix: fpre_
-- ============================================================
CREATE TABLE tb_formas_presentacion (
    fpre_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    fpre_codigo         VARCHAR(10) NOT NULL UNIQUE,
    fpre_nombre         VARCHAR(100) NOT NULL,
    fpre_created_at     TIMESTAMP NOT NULL,
    fpre_updated_at     TIMESTAMP,
    fpre_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_formas_presentacion (fpre_codigo, fpre_nombre, fpre_activo, fpre_created_at) VALUES
    ('BLISTER',  'Blister',            true, NOW()),
    ('FRASCO',   'Frasco',             true, NOW()),
    ('AMP7',     'Ampolla 1 mL',       true, NOW()),
    ('AMP3',     'Ampolla 3 mL',       true, NOW()),
    ('TUBO',     'Tubo',               true, NOW()),
    ('SACHET',   'Sachet',             true, NOW()),
    ('FCO_GOT',  'Frasco Gotero',      true, NOW()),
    ('JARABE',   'Frasco Jarabe',      true, NOW()),
    ('CREMA',    'Envase Crema',       true, NOW()),
    ('INY',      'Inyectable',         true, NOW());

-- ============================================================
-- Table: tb_grupos_farmacologicos
-- Prefix: gfar_
-- ============================================================
CREATE TABLE tb_grupos_farmacologicos (
    gfar_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    gfar_codigo         VARCHAR(10) NOT NULL UNIQUE,
    gfar_nombre         VARCHAR(100) NOT NULL,
    gfar_created_at     TIMESTAMP NOT NULL,
    gfar_updated_at     TIMESTAMP,
    gfar_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_grupos_farmacologicos (gfar_codigo, gfar_nombre, gfar_activo, gfar_created_at) VALUES
    ('AINE',     'Antiinflamatorio no Esteroideo',     true, NOW()),
    ('ANALG',    'Analgésico',                          true, NOW()),
    ('ANTIB',    'Antibiótico',                         true, NOW()),
    ('ANTIV',    'Antiviral',                           true, NOW()),
    ('ANTIH',    'Antihipertensivo',                    true, NOW()),
    ('DIURE',    'Diurético',                           true, NOW()),
    ('ANTID',    'Antidepresivo',                       true, NOW()),
    ('ANTIP',    'Antipsicótico',                       true, NOW()),
    ('ANTIA',    'Antiansiedad',                        true, NOW()),
    ('ANTIC',    'Anticonvulsivante',                   true, NOW()),
    ('ANTIHIS',  'Antihistamínico',                     true, NOW()),
    ('CORTI',    'Corticoide',                          true, NOW()),
    ('VIT',      'Vitamina',                            true, NOW()),
    ('SUPLE',    'Suplemento',                          true, NOW()),
    ('OTROS',    'Otros',                               true, NOW());

-- ============================================================
-- Table: tb_marcas
-- Prefix: marc_
-- ============================================================
CREATE TABLE tb_marcas (
    marc_id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    marc_codigo         VARCHAR(10) NOT NULL UNIQUE,
    marc_nombre         VARCHAR(100) NOT NULL,
    marc_created_at     TIMESTAMP NOT NULL,
    marc_updated_at     TIMESTAMP,
    marc_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_marcas (marc_codigo, marc_nombre, marc_activo, marc_created_at) VALUES
    ('GENFAR',  'Genfar',             true, NOW()),
    ('MK',      'Merck',              true, NOW()),
    ('PFIZER',  'Pfizer',             true, NOW()),
    ('BAYER',   'Bayer',              true, NOW()),
    ('ROEMM',   'Roemmers',           true, NOW()),
    ('ABBOTT',  'Abbott',             true, NOW()),
    ('SANDOZ',  'Sandoz',             true, NOW()),
    ('ACFAR',   'AC Farma',           true, NOW()),
    ('MEDIFAR', 'Medifarma',          true, NOW()),
    ('TEVA',    'Teva',               true, NOW());
