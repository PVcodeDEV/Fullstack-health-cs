CREATE TABLE tb_estados_civil (
    esc_id            BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    esc_codigo_reniec VARCHAR(3)  NOT NULL UNIQUE,
    esc_nombre        VARCHAR(50) NOT NULL,
    esc_created_at    TIMESTAMP NOT NULL,
    esc_updated_at    TIMESTAMP,
    esc_activo        BOOLEAN NOT NULL DEFAULT TRUE
);

-- Seed data: RENIEC-aligned civil statuses
INSERT INTO tb_estados_civil (esc_codigo_reniec, esc_nombre, esc_activo, esc_created_at) VALUES
    ('01', 'Soltero',     true, NOW()),
    ('02', 'Casado',      true, NOW()),
    ('03', 'Divorciado',  true, NOW()),
    ('04', 'Viudo',       true, NOW()),
    ('05', 'Conviviente', true, NOW());
