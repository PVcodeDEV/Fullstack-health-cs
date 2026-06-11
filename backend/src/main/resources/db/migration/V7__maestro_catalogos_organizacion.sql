-- Organization Catalogs: 4 tables + seed data

-- ============================================================
-- Table: tb_areas_funcionales
-- ============================================================
CREATE TABLE tb_areas_funcionales (
    areaf_id             SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    areaf_codigo         VARCHAR(10) NOT NULL UNIQUE,
    areaf_nombre         VARCHAR(100) NOT NULL,
    areaf_es_area_fisica BOOLEAN NOT NULL DEFAULT FALSE,
    areaf_created_at     TIMESTAMP NOT NULL,
    areaf_updated_at     TIMESTAMP,
    areaf_activo         BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_areas_funcionales (areaf_codigo, areaf_nombre, areaf_es_area_fisica, areaf_activo, areaf_created_at) VALUES
    ('ADM',  'Admisión',               true,  true, NOW()),
    ('SOP',  'Sala de Operaciones',     true,  true, NOW()),
    ('HOS',  'Hospitalización',        true,  true, NOW()),
    ('FAR',  'Farmacia',               true,  true, NOW()),
    ('CAJ',  'Caja',                   true,  true, NOW()),
    ('ENF',  'Enfermería',             false, true, NOW()),
    ('NUT',  'Nutrición',              false, true, NOW()),
    ('ADMN', 'Administración',         false, true, NOW()),
    ('SIS',  'Sistemas',               false, true, NOW());

-- ============================================================
-- Table: tb_aseguradoras (insurers / health insurance)
-- ============================================================
CREATE TABLE tb_aseguradoras (
    aseg_id              SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aseg_codigo          VARCHAR(10) NOT NULL UNIQUE,
    aseg_nombre          VARCHAR(150) NOT NULL,
    aseg_tipo            VARCHAR(10) NOT NULL,
    aseg_contrato_vigente BOOLEAN NOT NULL DEFAULT TRUE,
    aseg_created_at      TIMESTAMP NOT NULL,
    aseg_updated_at      TIMESTAMP,
    aseg_activo          BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_aseguradoras (aseg_codigo, aseg_nombre, aseg_tipo, aseg_contrato_vigente, aseg_activo, aseg_created_at) VALUES
    ('ESS', 'Essalud',         'PUBLICO', true, true, NOW()),
    ('SIS', 'SIS',             'PUBLICO', true, true, NOW()),
    ('RIM', 'Rímac EPS',       'PRIVADO', true, true, NOW()),
    ('PAC', 'Pacífico EPS',    'PRIVADO', true, true, NOW()),
    ('MAP', 'Mapfre EPS',      'PRIVADO', true, true, NOW()),
    ('LAV', 'La Victoria EPS', 'PRIVADO', true, true, NOW());

-- ============================================================
-- Table: tb_categorias_insumo (supply categories, self-ref FK)
-- ============================================================
CREATE TABLE tb_categorias_insumo (
    categ_id                 SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    categ_codigo             VARCHAR(10) NOT NULL UNIQUE,
    categ_nombre             VARCHAR(100) NOT NULL,
    categ_categoria_padre_id SMALLINT,
    categ_created_at         TIMESTAMP NOT NULL,
    categ_updated_at         TIMESTAMP,
    categ_activo             BOOLEAN NOT NULL DEFAULT TRUE
);

ALTER TABLE tb_categorias_insumo ADD CONSTRAINT fk_categ_padre
    FOREIGN KEY (categ_categoria_padre_id) REFERENCES tb_categorias_insumo(categ_id)
    ON DELETE RESTRICT;

INSERT INTO tb_categorias_insumo (categ_codigo, categ_nombre, categ_activo, categ_created_at) VALUES
    ('MED', 'Medicamento',           true, NOW()),
    ('MAT', 'Material Médico',       true, NOW()),
    ('INS', 'Insumo Quirúrgico',     true, NOW()),
    ('ESM', 'Material de Escritorio', true, NOW()),
    ('REA', 'Reactivo',              true, NOW()),
    ('OTR', 'Otros',                 true, NOW());

-- ============================================================
-- Table: tb_tipos_documento_clinico (clinical document types)
-- ============================================================
CREATE TABLE tb_tipos_documento_clinico (
    tdc_id              SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tdc_codigo          VARCHAR(10) NOT NULL UNIQUE,
    tdc_nombre          VARCHAR(100) NOT NULL,
    tdc_requiere_firma  BOOLEAN NOT NULL DEFAULT FALSE,
    tdc_created_at      TIMESTAMP NOT NULL,
    tdc_updated_at      TIMESTAMP,
    tdc_activo          BOOLEAN NOT NULL DEFAULT TRUE
);

INSERT INTO tb_tipos_documento_clinico (tdc_codigo, tdc_nombre, tdc_requiere_firma, tdc_activo, tdc_created_at) VALUES
    ('HC',  'Historia Clínica',        false, true, NOW()),
    ('EVO', 'Evolución',               true,  true, NOW()),
    ('ROP', 'Reporte Operatoria',      true,  true, NOW()),
    ('EPI', 'Epicrisis',               true,  true, NOW()),
    ('NEF', 'Nota de Enfermería',      true,  true, NOW()),
    ('KAR', 'Kardex',                  true,  true, NOW()),
    ('CON', 'Consentimiento Informado', true,  true, NOW()),
    ('REC', 'Receta Médica',           true,  true, NOW());
