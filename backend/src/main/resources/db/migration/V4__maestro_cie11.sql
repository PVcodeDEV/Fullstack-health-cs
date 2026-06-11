-- CIE-11 Diagnostic Codes Catalog
-- Schema only — seed data goes in V9__seed_cie11.sql
-- NOTE: For PostgreSQL production, add GIN trigram index on cie_descripcion:
--   CREATE INDEX IF NOT EXISTS idx_cie_desc_trgm
--       ON tb_cie11_diagnosticos USING gin (cie_descripcion gin_trgm_ops);
-- H2 does not support GIN indexes, so the index is applied separately for PG.

CREATE TABLE tb_cie11_diagnosticos (
    cie_id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cie_codigo           VARCHAR(8)   NOT NULL UNIQUE,
    cie_descripcion      VARCHAR(500) NOT NULL,
    cie_categoria        VARCHAR(1)   NOT NULL,
    cie_sexo_aplicable   VARCHAR(5)   NOT NULL DEFAULT 'AMBOS'
                         CHECK (cie_sexo_aplicable IN ('M', 'F', 'AMBOS')),
    cie_edad_minina      INT,
    cie_edad_maxima      INT,
    cie_version          VARCHAR(10)  NOT NULL DEFAULT 'CIE-11',
    cie_frecuencia_uso   INT          NOT NULL DEFAULT 0
);

-- BTREE index on codigo for autocomplete search
CREATE INDEX IF NOT EXISTS idx_cie_codigo
    ON tb_cie11_diagnosticos(cie_codigo);

-- BTREE index on categoria for filtering
CREATE INDEX IF NOT EXISTS idx_cie_categoria
    ON tb_cie11_diagnosticos(cie_categoria);
