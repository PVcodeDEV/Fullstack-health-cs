CREATE TABLE tb_ubigeo_departamentos (
    ubdep_codigo    VARCHAR(2)  PRIMARY KEY,
    ubdep_nombre    VARCHAR(100) NOT NULL,
    ubdep_created_at TIMESTAMP NOT NULL,
    ubdep_updated_at TIMESTAMP,
    ubdep_activo    BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tb_ubigeo_provincias (
    ubprov_codigo       VARCHAR(4)  PRIMARY KEY,
    ubprov_nombre       VARCHAR(100) NOT NULL,
    ubprov_departamento VARCHAR(2)  NOT NULL,
    ubprov_created_at   TIMESTAMP NOT NULL,
    ubprov_updated_at   TIMESTAMP,
    ubprov_activo       BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ubprov_departamento
        FOREIGN KEY (ubprov_departamento)
        REFERENCES tb_ubigeo_departamentos(ubdep_codigo)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_ubprov_departamento
    ON tb_ubigeo_provincias(ubprov_departamento);

CREATE TABLE tb_ubigeo_distritos (
    ubdist_codigo     VARCHAR(6)  PRIMARY KEY,
    ubdist_nombre     VARCHAR(100) NOT NULL,
    ubdist_provincia  VARCHAR(4)  NOT NULL,
    ubdist_created_at TIMESTAMP NOT NULL,
    ubdist_updated_at TIMESTAMP,
    ubdist_activo     BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_ubdist_provincia
        FOREIGN KEY (ubdist_provincia)
        REFERENCES tb_ubigeo_provincias(ubprov_codigo)
        ON DELETE RESTRICT
);

CREATE INDEX IF NOT EXISTS idx_ubdist_provincia
    ON tb_ubigeo_distritos(ubdist_provincia);
