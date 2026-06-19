# Portal Seguridad Specification

## Purpose

Portal Thymeleaf standalone para administración del módulo seguridad: usuarios, roles, permisos, configuración API. Sigue el mismo patrón que los portales existentes (asistencial, caja, farmacia) con tema indigo.

## Requirements

### Requirement: R-001 — Portal access

The portal MUST be mapped at `/seguridad/**.` The controller MUST use `@PreAuthorize("hasAnyAuthority('seguridad:ver', 'ROLE_ADMIN')")`. Unauthenticated requests MUST redirect to `/login`.

#### Scenario: Granted access
- GIVEN a Usuario with `seguridad:ver` authority
- WHEN GET `/seguridad/usuarios`
- THEN the portal renders the usuarios list view

#### Scenario: Denied access
- GIVEN a Usuario without `seguridad:ver` or `ROLE_ADMIN`
- WHEN GET `/seguridad/`
- THEN the system returns 403 Forbidden

### Requirement: R-002 — Dashboard

The dashboard at `/seguridad` MUST show summary counts: total usuarios, roles, permisos, and API config entries. The sidebar MUST highlight the active page with accent styling.

#### Scenario: Dashboard renders
- GIVEN an authenticated user with access
- WHEN GET `/seguridad`
- THEN the dashboard shows counts for usuarios, roles, permisos, config API
- AND the sidebar "Dashboard" link is highlighted

### Requirement: R-003 — Usuarios CRUD

The system MUST support list, create, edit, and role-assignment views for usuarios. The create form MUST include fields: username, password (BCrypt), persona selector, and role checkboxes.

#### Scenario: Create usuario with roles
- GIVEN an existing Persona and roles in the system
- WHEN POST `/seguridad/usuarios` with valid data and selected roles
- THEN the usuario is created with BCrypt hash and linked to the Persona and roles

#### Scenario: List usuarios
- GIVEN usuarios exist in the system
- WHEN GET `/seguridad/usuarios`
- THEN the list shows username, linked Persona, and assigned roles

### Requirement: R-004 — Roles CRUD

The portal MUST support list roles, create new roles, and manage permiso assignments per role via checkbox selection.

#### Scenario: Assign permisos to role
- GIVEN an existing role and available permisos
- WHEN updating the role's permisos via the UI
- THEN the `tb_roles_permisos` join table reflects the new assignments

### Requirement: R-005 — Permisos catalog

The portal MUST show a read-only list of all permisos with columns: código, nombre, módulo, descripción. Filter by módulo MUST be supported.

#### Scenario: Filter by module
- GIVEN permisos from different modules
- WHEN selecting "caja" filter
- THEN only caja-related permisos are displayed

### Requirement: R-006 — API config CRUD

The portal MUST support list, edit, and toggle enabled/disabled for `tb_configuracion_api` entries. Changes MUST persist immediately and reflect at runtime without restart.

#### Scenario: Edit API config value
- GIVEN a config entry with `clave=base_url` and `valor=https://old.url`
- WHEN updating the value to `https://new.url`
- THEN `tb_configuracion_api` is updated
- AND the next API call uses the new URL
