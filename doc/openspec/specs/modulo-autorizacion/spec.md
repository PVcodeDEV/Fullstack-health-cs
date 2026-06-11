# Autorización Specification

## Purpose

Role-based access control: Usuarios with FK to Persona and Trabajador, granular Permisos mapped to Roles, and runtime API config storage. Bootstrap admin creation on first startup.

## Requirements

### Requirement: R-001 — Usuario entity

`tb_usuarios` MUST have: `username` (unique), `password_hash` (BCrypt), `activo` flag, `persona_id` (FK → tb_personas, NOT NULL), and `trabajador_id` (FK → tb_trabajadores, nullable). PII fields MUST NOT appear in logs.

#### Scenario: SC-001-1 — Create with Persona
- GIVEN an existing Persona
- WHEN creating a Usuario with username, password, and `persona_id`
- THEN it is persisted with BCrypt hash and linked via FK

#### Scenario: SC-001-2 — Nullable trabajador
- GIVEN an admin who is not a clinic employee
- WHEN creating a Usuario without `trabajador_id`
- THEN creation succeeds with `trabajador_id` as NULL

### Requirement: R-002 — Rol catalog

The system MUST seed six roles on first startup: ADMIN, MEDICO, ENFERMERIA, RECEPCION, FARMACIA, CAJA. Seeding MUST be idempotent.

#### Scenario: SC-002-1 — First-run seeding
- GIVEN empty `tb_roles`
- WHEN the application starts
- THEN six roles are inserted

#### Scenario: SC-002-2 — Idempotent re-run
- GIVEN roles already exist in `tb_roles`
- WHEN the application starts again
- THEN no duplicate entries are created

### Requirement: R-003 — Granular permisos

Permisos MUST follow `{recurso}:{accion}` naming (e.g., `persona:crear`, `paciente:ver`). Each permiso MUST be unique by name. Roles MAY have multiple permisos via `tb_roles_permisos`.

#### Scenario: SC-003-1 — Role grants multiple permisos
- GIVEN ADMIN role with `persona:crear`, `persona:ver`, `persona:editar`
- WHEN assigning all three to ADMIN
- THEN ADMIN grants all three permissions

### Requirement: R-004 — Authorization via @PreAuthorize

Controllers MUST use `@PreAuthorize("hasAuthority('{permiso}')")` on secured endpoints. The system MUST enforce the authority check before method execution.

#### Scenario: SC-004-1 — Authorized access
- GIVEN a Usuario with role containing `persona:crear`
- WHEN POST `/api/v1/personas` with valid JWT
- THEN the request proceeds

#### Scenario: SC-004-2 — Unauthorized access
- GIVEN a Usuario without `persona:eliminar`
- WHEN DELETE `/api/v1/personas/{id}` with valid JWT
- THEN the system returns 403 Forbidden

### Requirement: R-005 — Runtime API config

`tb_configuracion_api` MUST store API provider config (base URL, token, enabled flag) with columns: `id`, `modulo`, `clave`, `valor`, `fecha_actualizacion`. Changes MUST reflect without restart.

#### Scenario: SC-005-1 — Config read from DB
- GIVEN a row with `modulo=reniec, clave=base_url, valor=https://api.reniec.gob.pe/v1`
- WHEN `ReniecClient.consultaDni()` is called
- THEN the system reads the URL from `tb_configuracion_api`

#### Scenario: SC-005-2 — Runtime update
- GIVEN `base_url=https://old.url` in `tb_configuracion_api`
- WHEN updated to `https://new.url`
- THEN the next call uses the new URL without restart

### Requirement: R-006 — Bootstrap admin

On first startup, the system MUST create an ADMIN Usuario from `ADMIN_USERNAME` and `ADMIN_PASSWORD` env vars with the ADMIN role. MUST be idempotent.

#### Scenario: SC-006-1 — Admin created
- GIVEN `ADMIN_USERNAME=admin`, `ADMIN_PASSWORD=securepass`
- WHEN the app starts with empty `tb_usuarios`
- THEN `admin` Usuario is created with ADMIN role and BCrypt hash

#### Scenario: SC-006-2 — Missing env vars
- GIVEN `ADMIN_USERNAME` is not set
- WHEN the application starts
- THEN a warning is logged and bootstrap is skipped
