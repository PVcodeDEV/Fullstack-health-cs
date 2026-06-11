-- ============================================================
-- V35: Farmacia - Add lote_version column for optimistic locking
-- 
-- @Version was incorrectly placed on lote_stock_actual, causing
-- Hibernate to auto-increment stock on every UPDATE. This migration
-- adds a dedicated version column so stock_actual behaves correctly.
-- ============================================================

ALTER TABLE tb_lotes ADD COLUMN lote_version INTEGER NOT NULL DEFAULT 0;
