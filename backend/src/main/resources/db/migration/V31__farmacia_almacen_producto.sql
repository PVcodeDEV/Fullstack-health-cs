-- ============================================================
-- V31: Farmacia - Almacen + Producto
-- ============================================================

-- ============================================================
-- Table: tb_almacenes
-- Prefix: alm_
-- ============================================================
CREATE TABLE tb_almacenes (
    alm_id              BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    alm_codigo          VARCHAR(20) NOT NULL UNIQUE,
    alm_nombre          VARCHAR(100) NOT NULL,
    alm_ubicacion       VARCHAR(255),
    alm_default         BOOLEAN NOT NULL DEFAULT FALSE,
    alm_created_at      TIMESTAMP NOT NULL,
    alm_updated_at      TIMESTAMP,
    alm_activo          BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed default warehouse
INSERT INTO tb_almacenes (alm_codigo, alm_nombre, alm_ubicacion, alm_default, alm_activo, alm_created_at)
    VALUES ('DEF', 'Almacén Principal', 'Sótano - Estante A1', true, true, NOW());

-- ============================================================
-- Table: tb_productos
-- Prefix: prod_
-- Common fields + type-specific columns
-- ============================================================
CREATE TABLE tb_productos (
    prod_id                     BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    prod_codigo                 VARCHAR(50) NOT NULL UNIQUE,
    prod_tipo                   VARCHAR(20) NOT NULL CHECK (prod_tipo IN ('MEDICAMENTO', 'INSUMO')),

    -- Common fields
    prod_precio_costo           DECIMAL(12,4) NOT NULL,
    prod_utilidad_medico        DECIMAL(5,2),
    prod_utilidad_publico       DECIMAL(5,2),
    prod_precio_venta_medico    DECIMAL(10,2),
    prod_precio_venta_publico   DECIMAL(10,2),
    prod_stock_minimo           INTEGER NOT NULL DEFAULT 0,
    prod_stock_critico          INTEGER NOT NULL DEFAULT 0,
    prod_categoria_insumo_id    BIGINT,
    prod_unidad_medida_id       SMALLINT,
    prod_activo                 BOOLEAN NOT NULL DEFAULT TRUE,

    -- MEDICAMENTO fields
    prod_generico               VARCHAR(255),
    prod_descripcion            TEXT,
    prod_origen                 BOOLEAN,
    prod_tipo_medicamento_id    BIGINT,
    prod_forma_farmaceutica_id  BIGINT,
    prod_forma_presentacion_id  BIGINT,
    prod_grupo_farmacologico_id BIGINT,

    -- INSUMO fields
    prod_marca_id               BIGINT,

    -- Audit
    prod_created_at             TIMESTAMP NOT NULL,
    prod_updated_at             TIMESTAMP,

    -- Constraints
    CONSTRAINT fk_prod_categoria_insumo FOREIGN KEY (prod_categoria_insumo_id)
        REFERENCES tb_categorias_insumo(categ_id),
    CONSTRAINT fk_prod_unidad_medida FOREIGN KEY (prod_unidad_medida_id)
        REFERENCES tb_unidades_medida(umed_id),
    CONSTRAINT fk_prod_tipo_medicamento FOREIGN KEY (prod_tipo_medicamento_id)
        REFERENCES tb_tipos_medicamento(tmed_id),
    CONSTRAINT fk_prod_forma_farmaceutica FOREIGN KEY (prod_forma_farmaceutica_id)
        REFERENCES tb_formas_farmaceuticas(ffar_id),
    CONSTRAINT fk_prod_forma_presentacion FOREIGN KEY (prod_forma_presentacion_id)
        REFERENCES tb_formas_presentacion(fpre_id),
    CONSTRAINT fk_prod_grupo_farmacologico FOREIGN KEY (prod_grupo_farmacologico_id)
        REFERENCES tb_grupos_farmacologicos(gfar_id),
    CONSTRAINT fk_prod_marca FOREIGN KEY (prod_marca_id)
        REFERENCES tb_marcas(marc_id)
);

CREATE INDEX idx_prod_codigo ON tb_productos(prod_codigo);
CREATE INDEX idx_prod_tipo ON tb_productos(prod_tipo);
CREATE INDEX idx_prod_activo ON tb_productos(prod_activo);
