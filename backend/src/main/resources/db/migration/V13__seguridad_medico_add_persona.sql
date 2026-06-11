-- =============================================================================
-- V13: Add med_persona_id to tb_medicos, make trabajador_id nullable
-- =============================================================================
-- External doctors are not employees but need a Persona reference.

-- Add direct person reference (all doctors must have a Persona)
ALTER TABLE tb_medicos ADD COLUMN med_persona_id BIGINT;

-- Backfill: for existing employee-doctors, copy persona from their trabajador
UPDATE tb_medicos m
SET med_persona_id = (SELECT tra_persona_id FROM tb_trabajadores t WHERE t.tra_id = m.med_trabajador_id)
WHERE med_trabajador_id IS NOT NULL;

-- Make med_persona_id NOT NULL once backfilled
ALTER TABLE tb_medicos ALTER COLUMN med_persona_id SET NOT NULL;

-- Add FK to tb_personas
ALTER TABLE tb_medicos ADD CONSTRAINT fk_med_persona FOREIGN KEY (med_persona_id) REFERENCES tb_personas(pers_persona_id);

-- Make trabajador_id nullable (external doctors have no trabajador)
ALTER TABLE tb_medicos ALTER COLUMN med_trabajador_id DROP NOT NULL;

-- Indexes
CREATE UNIQUE INDEX idx_med_persona_id ON tb_medicos(med_persona_id);
