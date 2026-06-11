-- Financial SUNAT Catalogs: 3 tables + seed data
-- All use SMALLINT PK + codigo_sunat UNIQUE (user-approved standard)

-- ============================================================
-- Table: tb_tipos_comprobante
-- SUNAT catalog: Factura (01), Boleta (03), NC (07), ND (08), LC (52)
-- ============================================================
CREATE TABLE tb_tipos_comprobante (
    tcomp_id            SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tcomp_codigo_sunat  VARCHAR(2)  NOT NULL UNIQUE,
    tcomp_nombre        VARCHAR(100) NOT NULL,
    tcomp_created_at    TIMESTAMP   NOT NULL,
    tcomp_updated_at    TIMESTAMP,
    tcomp_activo        BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_tipos_moneda
-- SUNAT catalog: PEN (Soles), USD (Dólares)
-- ============================================================
CREATE TABLE tb_tipos_moneda (
    tmon_id             SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tmon_codigo_sunat   VARCHAR(3)  NOT NULL UNIQUE,
    tmon_nombre         VARCHAR(100) NOT NULL,
    tmon_simbolo        VARCHAR(5)  NOT NULL,
    tmon_created_at     TIMESTAMP   NOT NULL,
    tmon_updated_at     TIMESTAMP,
    tmon_activo         BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ============================================================
-- Table: tb_unidades_medida
-- SUNAT catalog: NIU (Und), KGM (kg), GRM (g), LTR (L), etc.
-- ============================================================
CREATE TABLE tb_unidades_medida (
    umed_id             SMALLINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    umed_codigo_sunat   VARCHAR(5)  NOT NULL UNIQUE,
    umed_nombre         VARCHAR(100) NOT NULL,
    umed_abreviatura    VARCHAR(10),
    umed_created_at     TIMESTAMP   NOT NULL,
    umed_updated_at     TIMESTAMP,
    umed_activo         BOOLEAN     NOT NULL DEFAULT TRUE
);

-- ============================================================
-- SEED DATA
-- ============================================================

-- TipoComprobante (5 rows)
INSERT INTO tb_tipos_comprobante (tcomp_codigo_sunat, tcomp_nombre, tcomp_activo, tcomp_created_at) VALUES
    ('01', 'Factura',               true, NOW()),
    ('03', 'Boleta de Venta',       true, NOW()),
    ('07', 'Nota de Crédito',       true, NOW()),
    ('08', 'Nota de Débito',        true, NOW()),
    ('52', 'Liquidación de Compra', true, NOW());

-- TipoMoneda (2 rows)
INSERT INTO tb_tipos_moneda (tmon_codigo_sunat, tmon_nombre, tmon_simbolo, tmon_activo, tmon_created_at) VALUES
    ('PEN', 'Soles',   'S/', true, NOW()),
    ('USD', 'Dólares', '$',  true, NOW());

-- UnidadMedida (9 rows)
INSERT INTO tb_unidades_medida (umed_codigo_sunat, umed_nombre, umed_abreviatura, umed_activo, umed_created_at) VALUES
    ('NIU', 'Unidad (bienes)',     'Und', true, NOW()),
    ('KGM', 'Kilogramo',           'kg',  true, NOW()),
    ('GRM', 'Gramo',               'g',   true, NOW()),
    ('MTR', 'Metro',               'm',   true, NOW()),
    ('LTR', 'Litro',               'L',   true, NOW()),
    ('MLT', 'Mililitro',           'ml',  true, NOW()),
    ('C62', 'Caja',                'Cja', true, NOW()),
    ('BX',  'Blister',             'Bls', true, NOW()),
    ('BO',  'Botella',             'Bot', true, NOW());
