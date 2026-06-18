-- =============================================================================
-- V45__fix_admin_password_hash_v2.sql
-- Force-update the admin user's BCrypt hash to match password '72852927'
-- V44 tried a conditional update that might have matched 0 rows if the
-- database already had a different hash from a prior DataInitializer run.
-- This version updates unconditionally.
-- =============================================================================
UPDATE tb_usuarios
SET usu_password_hash = '$2a$10$LE41So7o9zvub0oPOYkXU.juidue2lUjpF3HmcclPL0S4P6f6c9ye'
WHERE usu_username = '72852927';
