# Delta for Autorización

## MODIFIED Requirements

### Requirement: R-002 — Rol catalog and seed permisos

The system MUST seed eight roles on first startup: ADMIN, GERENCIA, MEDICO, ENFERMERIA, RECEPCION, FARMACIA, CAJA, CONTABILIDAD. Seeding MUST be idempotent.

The system MUST seed five additional permisos: `seguridad:ver`, `numeracion:ver`, `numeracion:editar`, `tipo-movimiento:ver`, `tipo-movimiento:editar`. The `seguridad:ver` permiso MUST be assigned to the ADMIN role via `assignAllPermisosToAdmin()`, which already grants every permiso to ADMIN.
(Previously: Seeded six roles only, no seguridad/numeracion/tipo-movimiento permisos)

#### Scenario: SC-002-1 — First-run seeding
- GIVEN empty `tb_roles` and empty `tb_permisos`
- WHEN the application starts
- THEN eight roles are inserted
- AND five new permisos (`seguridad:ver`, `numeracion:ver`, `numeracion:editar`, `tipo-movimiento:ver`, `tipo-movimiento:editar`) are seeded

#### Scenario: SC-002-2 — Idempotent re-run
- GIVEN roles and permisos already exist in `tb_roles` and `tb_permisos`
- WHEN the application starts again
- THEN no duplicate entries are created

### Requirement: R-003 — Granular permisos

Permisos MUST follow `{recurso}:{accion}` naming (e.g., `persona:crear`, `paciente:ver`, `seguridad:ver`, `numeracion:editar`). Each permiso MUST be unique by name. Roles MAY have multiple permisos via `tb_roles_permisos`. New resources include `seguridad`, `numeracion`, and `tipo-movimiento`.
(Previously: Resources were only persona, paciente, trabajador, medico, usuario, rol, permiso, configuracion, cama, admision, hospitalizacion, sop, hce, cuenta, rrhh, entidad, caja, portal)

#### Scenario: SC-003-1 — Role grants multiple permisos
- GIVEN ADMIN role with `seguridad:ver`, `numeracion:ver`, `numeracion:editar`
- WHEN assigning all three to ADMIN
- THEN ADMIN grants all three permissions

## ADDED Requirements

### Requirement: R-007 — Numeración and TipoMovimiento seed in DataInitializer

The DataInitializer MUST seed five tipos de movimiento (ENTRADA, SALIDA, AJUSTE, TRANSFERENCIA, DEVOLUCION) for FARMACIA on first startup. Seeding MUST be idempotent.

#### Scenario: First-run seed tipos movimiento
- GIVEN empty `tb_tipos_movimiento`
- WHEN the application starts
- THEN five tipos de movimiento are inserted with `modulo=FARMACIA`
