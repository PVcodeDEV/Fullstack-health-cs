# Tipo Movimiento Specification

## Purpose

Gestión de tipos de movimiento como datos maestros. Define catálogo de movimientos de inventario (entradas, salidas, ajustes, transferencias, devoluciones) para los módulos farmacia, almacén y otros que requieran control de stock.

## Requirements

### Requirement: R-001 — TipoMovimiento entity

The system MUST provide `tb_tipos_movimiento` with columns: `id` (PK), `codigo` (VARCHAR, UNIQUE), `nombre` (VARCHAR), `modulo` (VARCHAR, e.g. "FARMACIA", "ALMACEN"), `descripcion` (VARCHAR, nullable), `activo` (BOOLEAN, default true). The `codigo` MUST be unique.

#### Scenario: Create tipo movimiento
- GIVEN no existing entry with `codigo=ENTRADA`
- WHEN inserting with `(codigo=ENTRADA, nombre=Entrada, modulo=FARMACIA)`
- THEN the row is persisted
- AND a duplicate `codigo` is rejected

### Requirement: R-002 — Seed data

On first startup, the system MUST seed five tipos de movimiento for FARMACIA: `ENTRADA`, `SALIDA`, `AJUSTE`, `TRANSFERENCIA`, `DEVOLUCION`. Seeding MUST be idempotent.

#### Scenario: First-run seed
- GIVEN empty `tb_tipos_movimiento`
- WHEN the application starts
- THEN five rows are inserted with `modulo=FARMACIA`

#### Scenario: Idempotent re-run
- GIVEN seed data already exists
- WHEN the application starts again
- THEN no duplicate entries are created

### Requirement: R-003 — V32 CHECK constraint fix

The migration MUST correct the existing V32 CHECK constraint on `tb_movimientos_inventario`: either remove the hardcoded enum CHECK entirely (since `tipo_movimiento` is now managed via FK or entity), or add `DEVOLUCION` to the CHECK list. The approach MUST be documented in the migration file.

#### Scenario: Insert DEVOLUCION still valid
- GIVEN the V32 CHECK constraint has been fixed
- WHEN inserting a movement with `tipo_movimiento=DEVOLUCION`
- THEN the insert succeeds without CHECK violation

### Requirement: R-004 — Admin UI in portal-seguridad

The portal-seguridad MUST provide views to list, create, edit, and toggle active for tipos de movimiento. The list MUST show codigo, nombre, modulo, and active status with module filter.

#### Scenario: Filter by module
- GIVEN tipos from FARMACIA and ALMACEN modules
- WHEN selecting "ALMACEN" filter
- THEN only ALMACEN entries are displayed

#### Scenario: Toggle active
- GIVEN an active tipo movimiento
- WHEN marking it inactive via UI
- THEN `activo=false`
- AND it should not appear in active dropdown selectors
