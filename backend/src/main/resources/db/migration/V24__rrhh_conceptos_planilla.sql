-- ============================================================================
-- V24: Payroll concepts catalog
-- ============================================================================
CREATE TABLE tb_conceptos_planilla (
    cpl_id          BIGINT GENERATED ALWAYS AS IDENTITY    PRIMARY KEY,
    cpl_codigo      VARCHAR(30)  NOT NULL UNIQUE,
    cpl_nombre      VARCHAR(100) NOT NULL,
    cpl_tipo        VARCHAR(20)  NOT NULL,
    cpl_formula     VARCHAR(100),
    cpl_orden       INTEGER      NOT NULL DEFAULT 0,
    cpl_activo      BOOLEAN      NOT NULL DEFAULT TRUE,
    cpl_created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    cpl_updated_at  TIMESTAMP,
    CONSTRAINT ck_cpl_tipo CHECK (cpl_tipo IN ('INGRESO', 'DESCUENTO', 'APORTE'))
);

INSERT INTO tb_conceptos_planilla (cpl_codigo, cpl_nombre, cpl_tipo, cpl_orden) VALUES
    ('BASICO', 'Sueldo Base', 'INGRESO', 1),
    ('ASIGNACION_FAMILIAR', 'Asignación Familiar', 'INGRESO', 2),
    ('AFP_OBLIGATORIO', 'AFP Obligatorio', 'DESCUENTO', 10),
    ('ONP_DESCUENTO', 'ONP Descuento', 'DESCUENTO', 11),
    ('ESSALUD_APORTE', 'EsSalud Aporte', 'APORTE', 20),
    ('RENTA_5TA', 'Renta 5ta Categoría', 'DESCUENTO', 30),
    ('ADELANTO', 'Adelanto de Sueldo', 'DESCUENTO', 40);
