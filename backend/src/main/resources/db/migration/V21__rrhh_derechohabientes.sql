CREATE TABLE tb_derechohabientes (
    der_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    der_trabajador_id BIGINT NOT NULL REFERENCES tb_trabajadores(tra_id),
    der_persona_id BIGINT NOT NULL REFERENCES tb_personas(pers_persona_id),
    der_relacion VARCHAR(20) NOT NULL,
    der_fecha_inicio DATE NOT NULL,
    der_fecha_fin DATE,
    der_estado VARCHAR(20) NOT NULL DEFAULT 'ACTIVO',
    der_activo BOOLEAN NOT NULL DEFAULT TRUE,
    der_created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    der_updated_at TIMESTAMP,
    CONSTRAINT fk_der_trabajador FOREIGN KEY (der_trabajador_id) REFERENCES tb_trabajadores(tra_id),
    CONSTRAINT fk_der_persona FOREIGN KEY (der_persona_id) REFERENCES tb_personas(pers_persona_id),
    CONSTRAINT ck_der_relacion CHECK (der_relacion IN ('CONYUGE', 'HIJO', 'CONCUBINO', 'PADRE', 'MADRE')),
    CONSTRAINT ck_der_estado CHECK (der_estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE INDEX idx_der_trabajador ON tb_derechohabientes(der_trabajador_id);
CREATE INDEX idx_der_persona ON tb_derechohabientes(der_persona_id);
CREATE INDEX idx_der_estado ON tb_derechohabientes(der_estado);
