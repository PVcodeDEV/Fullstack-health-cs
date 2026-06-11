-- ============================================================
-- V36: Farmacia - Add BORRADOR EstadoVenta + conImpresion field
-- ============================================================

-- Add BORRADOR to the CHECK constraint on vent_estado
ALTER TABLE tb_ventas DROP CONSTRAINT IF EXISTS chk_vent_estado;
ALTER TABLE tb_ventas ADD CONSTRAINT chk_vent_estado CHECK (vent_estado IN ('BORRADOR','COMPLETADA','ANULADA'));

-- Add conImpresion field (CRITICAL-3)
ALTER TABLE tb_ventas ADD COLUMN vent_con_impresion BOOLEAN NOT NULL DEFAULT FALSE;
