-- =============================================================================
-- V14: Clinica — Camas y Habitaciones
-- =============================================================================

-- tb_habitaciones
CREATE TABLE tb_habitaciones (
    hab_id                  BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    hab_codigo              VARCHAR(20)  NOT NULL UNIQUE,
    hab_nombre              VARCHAR(100) NOT NULL,
    hab_tipo_habitacion_id  BIGINT       NOT NULL,
    hab_piso                INTEGER,
    hab_capacidad           INTEGER      NOT NULL DEFAULT 1,
    hab_created_at          TIMESTAMP    NOT NULL,
    hab_updated_at          TIMESTAMP,
    hab_activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_hab_tipo_habitacion FOREIGN KEY (hab_tipo_habitacion_id)
        REFERENCES tb_tipos_habitacion(thab_id)
);

-- tb_camas
CREATE TABLE tb_camas (
    cama_id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    cama_habitacion_id    BIGINT      NOT NULL,
    cama_codigo           VARCHAR(20) NOT NULL UNIQUE,
    cama_estado           VARCHAR(20) NOT NULL DEFAULT 'DISPONIBLE',
    cama_created_at       TIMESTAMP   NOT NULL,
    cama_updated_at       TIMESTAMP,
    cama_activo           BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_cama_habitacion FOREIGN KEY (cama_habitacion_id)
        REFERENCES tb_habitaciones(hab_id),
    CONSTRAINT ck_cama_estado CHECK (cama_estado IN ('DISPONIBLE', 'OCUPADO', 'MANTENIMIENTO'))
);

-- Indexes
CREATE INDEX idx_hab_tipo_habitacion ON tb_habitaciones(hab_tipo_habitacion_id);
CREATE INDEX idx_hab_piso ON tb_habitaciones(hab_piso);
CREATE INDEX idx_cama_habitacion ON tb_camas(cama_habitacion_id);
CREATE INDEX idx_cama_estado ON tb_camas(cama_estado);
CREATE INDEX idx_cama_activo ON tb_camas(cama_activo);

-- Seed: Habitaciones
INSERT INTO tb_habitaciones (hab_codigo, hab_nombre, hab_tipo_habitacion_id, hab_piso, hab_capacidad, hab_created_at, hab_activo)
VALUES
    ('HAB-101', 'Habitación 101 — Privada Individual',   1, 1, 1, NOW(), true),
    ('HAB-102', 'Habitación 102 — Privada Individual',   1, 1, 1, NOW(), true),
    ('HAB-103', 'Habitación 103 — Privada Suite',        4, 1, 1, NOW(), true),
    ('HAB-201', 'Habitación 201 — Compartida 2 camas',   2, 2, 2, NOW(), true),
    ('HAB-202', 'Habitación 202 — Compartida 2 camas',   2, 2, 2, NOW(), true),
    ('HAB-301', 'Habitación 301 — Compartida 3 camas',   3, 3, 3, NOW(), true),
    ('HAB-302', 'Habitación 302 — Compartida 3 camas',   3, 3, 3, NOW(), true);

-- Seed: Camas (one per capacity for shared, one each for private/suite)
INSERT INTO tb_camas (cama_habitacion_id, cama_codigo, cama_estado, cama_created_at, cama_activo)
VALUES
    (1, 'CAMA-101-1', 'DISPONIBLE', NOW(), true),
    (2, 'CAMA-102-1', 'DISPONIBLE', NOW(), true),
    (3, 'CAMA-103-1', 'DISPONIBLE', NOW(), true),
    (4, 'CAMA-201-1', 'DISPONIBLE', NOW(), true),
    (4, 'CAMA-201-2', 'DISPONIBLE', NOW(), true),
    (5, 'CAMA-202-1', 'DISPONIBLE', NOW(), true),
    (5, 'CAMA-202-2', 'DISPONIBLE', NOW(), true),
    (6, 'CAMA-301-1', 'DISPONIBLE', NOW(), true),
    (6, 'CAMA-301-2', 'DISPONIBLE', NOW(), true),
    (6, 'CAMA-301-3', 'DISPONIBLE', NOW(), true),
    (7, 'CAMA-302-1', 'DISPONIBLE', NOW(), true),
    (7, 'CAMA-302-2', 'DISPONIBLE', NOW(), true),
    (7, 'CAMA-302-3', 'DISPONIBLE', NOW(), true);
