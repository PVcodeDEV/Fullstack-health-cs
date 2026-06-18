-- =============================================================================
-- V44__fix_admin_password_hash.sql
-- Fix the admin user's BCrypt hash to match password '72852927'
-- Only updates if current hash is the known-incorrect one from seed.sql
-- =============================================================================
UPDATE tb_usuarios
SET usu_password_hash = '$2a$10$LE41So7o9zvub0oPOYkXU.juidue2lUjpF3HmcclPL0S4P6f6c9ye'
WHERE usu_username = '72852927'
  AND usu_password_hash = '$2a$10$23ufmWHMoOIwkJ0WXiNHnuMNCy2clvT6/VPGIW4qjkaa5gNCsNiX6';
