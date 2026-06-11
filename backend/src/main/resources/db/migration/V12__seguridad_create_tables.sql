-- =============================================================================
-- V12: Create seguridad module tables
-- =============================================================================

-- tb_usuarios
CREATE TABLE tb_usuarios (
    usu_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    usu_persona_id BIGINT NOT NULL UNIQUE,
    usu_trabajador_id BIGINT UNIQUE,
    usu_username VARCHAR(50) NOT NULL UNIQUE,
    usu_password_hash VARCHAR(255) NOT NULL,
    usu_last_login TIMESTAMP,
    usu_created_at TIMESTAMP NOT NULL,
    usu_updated_at TIMESTAMP,
    usu_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_usu_persona FOREIGN KEY (usu_persona_id) REFERENCES tb_personas(pers_persona_id),
    CONSTRAINT fk_usu_trabajador FOREIGN KEY (usu_trabajador_id) REFERENCES tb_trabajadores(tra_id)
);

-- tb_roles
CREATE TABLE tb_roles (
    rol_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    rol_codigo VARCHAR(30) NOT NULL UNIQUE,
    rol_nombre VARCHAR(100) NOT NULL,
    rol_descripcion VARCHAR(255),
    rol_created_at TIMESTAMP NOT NULL,
    rol_updated_at TIMESTAMP,
    rol_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- tb_permisos
CREATE TABLE tb_permisos (
    per_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    per_codigo VARCHAR(50) NOT NULL UNIQUE,
    per_nombre VARCHAR(100) NOT NULL,
    per_modulo VARCHAR(50) NOT NULL,
    per_descripcion VARCHAR(255),
    per_created_at TIMESTAMP NOT NULL,
    per_updated_at TIMESTAMP,
    per_activo BOOLEAN NOT NULL DEFAULT TRUE
);

-- tb_roles_permisos
CREATE TABLE tb_roles_permisos (
    rop_rol_id BIGINT NOT NULL,
    rop_permiso_id BIGINT NOT NULL,
    PRIMARY KEY (rop_rol_id, rop_permiso_id),
    CONSTRAINT fk_rop_rol FOREIGN KEY (rop_rol_id) REFERENCES tb_roles(rol_id),
    CONSTRAINT fk_rop_permiso FOREIGN KEY (rop_permiso_id) REFERENCES tb_permisos(per_id)
);

-- tb_usuarios_roles
CREATE TABLE tb_usuarios_roles (
    usro_usuario_id BIGINT NOT NULL,
    usro_rol_id BIGINT NOT NULL,
    PRIMARY KEY (usro_usuario_id, usro_rol_id),
    CONSTRAINT fk_usro_usuario FOREIGN KEY (usro_usuario_id) REFERENCES tb_usuarios(usu_id),
    CONSTRAINT fk_usro_rol FOREIGN KEY (usro_rol_id) REFERENCES tb_roles(rol_id)
);

-- tb_configuracion_api
CREATE TABLE tb_configuracion_api (
    conf_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    conf_modulo VARCHAR(50) NOT NULL,
    conf_clave VARCHAR(100) NOT NULL,
    conf_valor TEXT,
    conf_tipo VARCHAR(20) NOT NULL DEFAULT 'string',
    conf_created_at TIMESTAMP NOT NULL,
    conf_updated_at TIMESTAMP,
    conf_activo BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uq_conf_modulo_clave UNIQUE (conf_modulo, conf_clave)
);

-- Indexes
CREATE INDEX idx_usu_username ON tb_usuarios(usu_username);
CREATE INDEX idx_rol_codigo ON tb_roles(rol_codigo);
CREATE INDEX idx_per_codigo ON tb_permisos(per_codigo);
CREATE INDEX idx_per_modulo ON tb_permisos(per_modulo);
CREATE INDEX idx_conf_modulo ON tb_configuracion_api(conf_modulo);
