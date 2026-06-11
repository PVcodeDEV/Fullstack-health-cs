-- ============================================================================
-- V22: RRHH fixes — fechaIngreso NOT NULL + contrato motivo_cese
-- ============================================================================

-- Backfill NULL fechaIngreso from initial periodo laboral
UPDATE tb_trabajadores t
SET tra_fecha_ingreso = (
    SELECT pla_fecha_inicio
    FROM tb_periodos_laborales
    WHERE pla_trabajador_id = t.tra_id
    ORDER BY pla_fecha_inicio ASC
    LIMIT 1
)
WHERE tra_fecha_ingreso IS NULL;

-- Now set NOT NULL
ALTER TABLE tb_trabajadores
    ALTER COLUMN tra_fecha_ingreso SET NOT NULL;

-- Add motivo_cese to contratos
ALTER TABLE tb_contratos
    ADD COLUMN con_motivo_cese VARCHAR(100);
