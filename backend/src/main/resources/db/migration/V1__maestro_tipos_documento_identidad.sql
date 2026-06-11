CREATE TABLE tb_tipos_documento_identidad (
    tdi_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    tdi_codigo_sunat VARCHAR(5)  NOT NULL UNIQUE,
    tdi_nombre        VARCHAR(100) NOT NULL,
    tdi_longitud_minima INTEGER NOT NULL,
    tdi_longitud_maxima INTEGER NOT NULL,
    tdi_created_at    TIMESTAMP NOT NULL,
    tdi_updated_at    TIMESTAMP,
    tdi_activo        BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed data: SUNAT-aligned document types
INSERT INTO tb_tipos_documento_identidad (tdi_codigo_sunat, tdi_nombre, tdi_longitud_minima, tdi_longitud_maxima, tdi_activo, tdi_created_at) VALUES
    ('01', 'DNI',                       8,  8,  true, NOW()),
    ('04', 'Carnet de Extranjería',     12, 12, true, NOW()),
    ('06', 'RUC',                       11, 11, true, NOW()),
    ('07', 'Pasaporte',                 1,  20, true, NOW()),
    ('11', 'Carné de Extranjería',      1,  20, true, NOW());
