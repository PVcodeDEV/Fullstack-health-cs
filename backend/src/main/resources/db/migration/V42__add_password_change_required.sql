-- =============================================================================
-- V42: Add password_change_required column to tb_usuarios
-- =============================================================================
-- All new users must change password on first login.
-- Existing users get false (already using the system).

ALTER TABLE tb_usuarios
    ADD COLUMN usu_password_change_required BOOLEAN NOT NULL DEFAULT TRUE;

COMMENT ON COLUMN tb_usuarios.usu_password_change_required
    IS 'If true, user must change password on next login (Ley 29733 data privacy compliance)';
