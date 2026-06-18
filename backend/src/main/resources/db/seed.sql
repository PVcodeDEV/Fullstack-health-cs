-- =============================================================================
-- ERP Clínico — Seed Data
-- =============================================================================
-- Run AFTER Flyway migrations have created all tables.
-- This replaces the Java DataInitializer for bootstrap data.
--
-- Usage: PGPASSWORD=csosi psql -U csosi -d csuarezdb -f seed.sql
-- =============================================================================

BEGIN;

-- =============================================================================
-- 1. ROLES
-- =============================================================================
INSERT INTO tb_roles (rol_codigo, rol_nombre, rol_descripcion, rol_created_at, rol_activo) VALUES
    ('ADMIN',          'Administrador',  'Acceso total al sistema',                                   NOW(), true),
    ('GERENCIA',       'Gerencia',       'Acceso a reportes, aprobaciones y supervisión',             NOW(), true),
    ('MEDICO',         'Médico',         'Acceso a módulos clínicos y pacientes',                     NOW(), true),
    ('ENFERMERIA',     'Enfermería',     'Acceso a módulos de enfermería y pacientes',                NOW(), true),
    ('RECEPCION',      'Recepción',      'Acceso a admisión y registro de pacientes',                 NOW(), true),
    ('FARMACIA',       'Farmacia',       'Acceso al módulo de farmacia e inventario',                 NOW(), true),
    ('CAJA',           'Caja',           'Acceso al módulo de caja y facturación',                    NOW(), true),
    ('CONTABILIDAD',   'Contabilidad',   'Acceso al módulo de contabilidad',                          NOW(), true)
ON CONFLICT (rol_codigo) DO NOTHING;

-- =============================================================================
-- 2. PERMISOS
-- =============================================================================
INSERT INTO tb_permisos (per_codigo, per_nombre, per_modulo, per_descripcion, per_created_at, per_activo) VALUES

    -- Persona / Paciente / Trabajador / Médico
    ('persona:*',         'Acceso total a Personas',           'persona',       'Permiso total para el módulo Personas',                    NOW(), true),
    ('paciente:*',        'Acceso total a Pacientes',          'paciente',      'Permiso total para el módulo Pacientes',                   NOW(), true),
    ('trabajador:*',      'Acceso total a Trabajadores',       'trabajador',    'Permiso total para el módulo Trabajadores',                 NOW(), true),
    ('medico:*',          'Acceso total a Médicos',            'medico',        'Permiso total para el módulo Médicos',                      NOW(), true),

    -- Seguridad
    ('usuario:*',         'Acceso total a Usuarios',           'usuario',       'Permiso total para el módulo Usuarios',                     NOW(), true),
    ('rol:*',             'Acceso total a Roles',              'rol',           'Permiso total para el módulo Roles',                        NOW(), true),
    ('permiso:*',         'Acceso total a Permisos',           'permiso',       'Permiso total para el módulo Permisos',                     NOW(), true),
    ('configuracion:*',   'Acceso total a Configuración',      'configuracion', 'Permiso total para el módulo Configuración',                NOW(), true),

    -- Cama / Habitación
    ('cama:editar',       'Editar camas y habitaciones',       'cama',          'Edición de camas y habitaciones',                           NOW(), true),
    ('cama:ver',          'Ver camas y habitaciones',          'cama',          'Visualización de camas y habitaciones',                     NOW(), true),

    -- Admisión
    ('admision:editar',   'Editar admisiones',                 'admision',      'Edición de admisiones',                                     NOW(), true),
    ('admision:ver',      'Ver admisiones',                    'admision',      'Visualización de admisiones',                               NOW(), true),

    -- Hospitalización
    ('hospitalizacion:editar', 'Editar hospitalizaciones',     'hospitalizacion', 'Edición de hospitalizaciones',                             NOW(), true),
    ('hospitalizacion:ver',    'Ver hospitalizaciones',        'hospitalizacion', 'Visualización de hospitalizaciones',                       NOW(), true),

    -- SOP
    ('sop:editar',        'Editar reportes quirúrgicos',       'sop',           'Edición de reportes quirúrgicos',                           NOW(), true),
    ('sop:ver',           'Ver reportes quirúrgicos',          'sop',           'Visualización de reportes quirúrgicos',                     NOW(), true),

    -- HCE
    ('hce:editar',        'Editar documentos clínicos',        'hce',           'Edición de documentos clínicos',                            NOW(), true),
    ('hce:ver',           'Ver documentos clínicos',           'hce',           'Visualización de documentos clínicos',                      NOW(), true),

    -- Cuenta
    ('cuenta:editar',     'Editar cargos en cuenta',           'cuenta',        'Edición de cargos',                                         NOW(), true),
    ('cuenta:ver',        'Ver cuentas y cargos',              'cuenta',        'Visualización de cuentas y cargos',                         NOW(), true),

    -- RRHH
    ('rrhh:ver',          'Ver RRHH',                          'rrhh',          'Visualización de datos de RRHH',                            NOW(), true),
    ('rrhh:editar',       'Editar RRHH',                       'rrhh',          'Creación y edición de datos de RRHH',                       NOW(), true),
    ('rrhh:contrato:gestionar',        'Gestionar contratos',  'rrhh',          'Suspender, reactivar y resolver contratos',                 NOW(), true),
    ('rrhh:derechohabiente:gestionar', 'Gestionar derechohabientes', 'rrhh',    'Gestión de derechohabientes',                               NOW(), true),

    -- Entidad / Empresa
    ('entidad:crear',     'Crear empresas',                    'entidad',       'Creación de empresas/entidades',                            NOW(), true),
    ('entidad:ver',       'Ver empresas',                      'entidad',       'Visualización de empresas/entidades',                       NOW(), true),
    ('entidad:editar',    'Editar empresas',                   'entidad',       'Edición de empresas/entidades',                             NOW(), true),
    ('entidad:consultar-sunat', 'Consultar SUNAT RUC',         'entidad',       'Consulta de RUC en SUNAT',                                  NOW(), true),

    -- Caja
    ('caja:crear',        'Crear en caja',                     'caja',          'Creación de sesiones, tarifarios, comprobantes',            NOW(), true),
    ('caja:ver',          'Ver caja',                          'caja',          'Visualización de módulo caja',                              NOW(), true),
    ('caja:editar',       'Editar caja',                       'caja',          'Edición de registros de caja',                              NOW(), true),
    ('caja:aprobar',      'Aprobar operaciones de caja',       'caja',          'Aprobación de descuentos y operaciones',                    NOW(), true),
    ('caja:anular',       'Anular comprobantes',               'caja',          'Anulación de comprobantes vía Nota de Crédito',             NOW(), true),

    -- Portal access
    ('asistencial:ver',   'Acceder al Portal Asistencial',     'portal',        'Acceso al módulo Asistencial (pacientes, admisiones, HCE)', NOW(), true),
    ('farmacia:ver',      'Acceder al Portal Farmacia',        'portal',        'Acceso al módulo Farmacia (despacho, stock)',               NOW(), true),
    ('administrativo:ver','Acceder al Portal Administrativo',  'portal',        'Acceso al módulo Administrativo (RRHH, maestros, usuarios)',NOW(), true),
    ('seguridad:ver',     'Acceder al módulo Seguridad',       'portal',        'Acceso al módulo Seguridad (usuarios, roles, permisos)',    NOW(), true)

ON CONFLICT (per_codigo) DO NOTHING;

-- =============================================================================
-- 3. PERSONA ADMIN
-- =============================================================================
-- Data obtained via SUNAT API (DNI: 72852927)
INSERT INTO tb_personas (
    pers_tipo_documento_id, pers_numero_documento,
    pers_nombres, pers_apellido_paterno, pers_apellido_materno,
    pers_created_at, pers_activo
)
SELECT
    tdi.tdi_id, '72852927',
    'LUIS PATRICK ANTONY', 'VIGO', 'VILCHEZ',
    NOW(), true
FROM tb_tipos_documento_identidad tdi
WHERE tdi.tdi_codigo_sunat = '01'
AND NOT EXISTS (SELECT 1 FROM tb_personas WHERE pers_numero_documento = '72852927');

-- =============================================================================
-- 4. USUARIO ADMIN
-- =============================================================================
-- Username = DNI (72852927)
-- Password = DNI (72852927) — must change on first login
-- BCrypt hash generated for '72852927'
INSERT INTO tb_usuarios (
    usu_persona_id, usu_username, usu_password_hash,
    usu_password_change_required, usu_created_at, usu_activo
)
SELECT
    p.pers_persona_id, '72852927',
    '$2a$10$23ufmWHMoOIwkJ0WXiNHnuMNCy2clvT6/VPGIW4qjkaa5gNCsNiX6',
    true, NOW(), true
FROM tb_personas p
WHERE p.pers_numero_documento = '72852927'
AND NOT EXISTS (SELECT 1 FROM tb_usuarios WHERE usu_username = '72852927');

-- =============================================================================
-- 5. ASIGNAR PERMISOS A ROLES
-- =============================================================================

-- 5.1 ADMIN → ALL PERMISOS
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.2 CAJA → caja + entidad permisos
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'CAJA'
AND p.per_codigo IN ('caja:crear','caja:ver','caja:editar','caja:aprobar','caja:anular',
                     'entidad:crear','entidad:ver','entidad:editar','entidad:consultar-sunat')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.3 GERENCIA → approve discounts, view caja, view entities
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'GERENCIA'
AND p.per_codigo IN ('caja:aprobar','caja:ver','caja:editar',
                     'entidad:ver','entidad:editar',
                     'cuenta:ver')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.4 RECEPCION → read entities, read caja, read cuenta, admision + portal asistencial
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'RECEPCION'
AND p.per_codigo IN ('asistencial:ver','entidad:ver','caja:ver','cuenta:ver',
                     'admision:ver','admision:editar')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.5 MEDICO → clinical read, caja ver (pre-liquidación) + portal asistencial
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'MEDICO'
AND p.per_codigo IN ('asistencial:ver','caja:ver','cuenta:ver','admision:ver',
                     'hospitalizacion:ver','sop:ver','hce:ver','hce:editar')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.6 FARMACIA → farmacia permissions + portal farmacia
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'FARMACIA'
AND p.per_codigo IN ('farmacia:ver')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.7 ENFERMERIA → portal asistencial
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'ENFERMERIA'
AND p.per_codigo IN ('asistencial:ver')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- 5.8 CONTABILIDAD → portal administrativo
INSERT INTO tb_roles_permisos (rop_rol_id, rop_permiso_id)
SELECT r.rol_id, p.per_id
FROM tb_roles r, tb_permisos p
WHERE r.rol_codigo = 'CONTABILIDAD'
AND p.per_codigo IN ('administrativo:ver')
AND NOT EXISTS (
    SELECT 1 FROM tb_roles_permisos rp
    WHERE rp.rop_rol_id = r.rol_id AND rp.rop_permiso_id = p.per_id
);

-- =============================================================================
-- 6. ASIGNAR ROL ADMIN AL USUARIO ADMIN
-- =============================================================================
INSERT INTO tb_usuarios_roles (usro_usuario_id, usro_rol_id)
SELECT u.usu_id, r.rol_id
FROM tb_usuarios u, tb_roles r
WHERE u.usu_username = '72852927'
AND r.rol_codigo = 'ADMIN'
AND NOT EXISTS (
    SELECT 1 FROM tb_usuarios_roles ur
    WHERE ur.usro_usuario_id = u.usu_id AND ur.usro_rol_id = r.rol_id
);

COMMIT;
