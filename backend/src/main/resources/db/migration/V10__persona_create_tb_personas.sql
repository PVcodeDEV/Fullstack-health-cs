CREATE TABLE tb_personas (
    pers_persona_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    pers_tipo_documento_id     BIGINT       NOT NULL,
    pers_numero_documento      VARCHAR(20)  NOT NULL,
    pers_nombres               VARCHAR(200),
    pers_apellido_paterno      VARCHAR(100),
    pers_apellido_materno      VARCHAR(100),
    pers_fecha_nacimiento      DATE,
    pers_sexo                  CHAR(1),
    pers_estado_civil_id       BIGINT,
    pers_direccion             VARCHAR(255),
    pers_ubigeo_distrito       VARCHAR(6),
    pers_telefono              VARCHAR(20),
    pers_email                 VARCHAR(100),
    pers_fecha_ultima_consulta DATE,
    pers_created_at            TIMESTAMP    NOT NULL,
    pers_updated_at            TIMESTAMP,
    pers_activo                BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_pers_tipo_documento FOREIGN KEY (pers_tipo_documento_id)
        REFERENCES tb_tipos_documento_identidad(tdi_id),
    CONSTRAINT fk_pers_estado_civil FOREIGN KEY (pers_estado_civil_id)
        REFERENCES tb_estados_civil(esc_id),
    CONSTRAINT ck_pers_sexo CHECK (pers_sexo IN ('M', 'F'))
);

CREATE UNIQUE INDEX idx_pers_numero_documento ON tb_personas(pers_numero_documento);
CREATE INDEX idx_pers_tipo_documento_id ON tb_personas(pers_tipo_documento_id);
CREATE INDEX idx_pers_nombres_apellidos ON tb_personas(pers_nombres, pers_apellido_paterno, pers_apellido_materno);
CREATE INDEX idx_pers_ubigeo_distrito ON tb_personas(pers_ubigeo_distrito);
CREATE INDEX idx_pers_fecha_ultima_consulta ON tb_personas(pers_fecha_ultima_consulta);
CREATE INDEX idx_pers_activo ON tb_personas(pers_activo);
