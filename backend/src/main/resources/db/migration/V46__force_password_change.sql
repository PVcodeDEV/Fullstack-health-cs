-- =============================================================================
-- V46__force_password_change.sql
-- Force password change for existing users on next login
-- =============================================================================
UPDATE tb_usuarios
SET usu_password_change_required = true
WHERE usu_password_change_required = false;
