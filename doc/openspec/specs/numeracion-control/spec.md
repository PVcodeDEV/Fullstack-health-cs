# Numeración Control Specification

## Purpose

Servicio centralizado de numeración correlativa sin saltos. Administra secuencias únicas por entidad, serie y año, garantizando que no se salten ni dupliquen números dentro de una serie.

## Requirements

### Requirement: R-001 — NumeracionControl entity

The system MUST provide `tb_numeracion_control` with columns: `id` (PK), `entidad` (VARCHAR, e.g. "COMPROBANTE"), `serie` (VARCHAR, e.g. "001"), `correlativo_actual` (INT), `prefijo` (VARCHAR, nullable), `longitud_correlativo` (INT, default 6), `anio` (INT), `activo` (BOOLEAN default true). A UNIQUE constraint MUST exist on `(entidad, serie, anio)`.

#### Scenario: Create numeración entry
- GIVEN a clean `tb_numeracion_control`
- WHEN inserting `(entidad=COMPROBANTE, serie=001, anio=2026)`
- THEN the row is created with `correlativo_actual=0`
- AND a duplicate `(entidad=COMPROBANTE, serie=001, anio=2026)` is rejected

### Requirement: R-002 — Gapless next correlativo

The service MUST provide `nextCorrelativo(entidad, serie)` that returns the next number as `{prefijo}{correlativo_formateado}`. The increment MUST use `SELECT ... FOR UPDATE` within a single `@Transactional` method to guarantee no gaps. Concurrent calls for the same `(entidad, serie)` MUST be serialized.

#### Scenario: Sequential numbers
- GIVEN `correlativo_actual=5` for `(COMPROBANTE, 001, 2026)`
- WHEN `nextCorrelativo("COMPROBANTE", "001")` is called
- THEN it returns `000006` (longitud=6, prefijo=null)
- AND `correlativo_actual` is updated to 6

#### Scenario: With prefijo
- GIVEN `correlativo_actual=10, prefijo="HC-", longitud=6, entidad=HC`
- WHEN `nextCorrelativo("HC", "001")` is called
- THEN it returns `HC-000011`

#### Scenario: Concurrent calls serialized
- GIVEN two threads call `nextCorrelativo("COMPROBANTE", "001")` simultaneously
- WHEN both execute
- THEN thread A returns N, thread B returns N+1
- AND no gap exists between the two numbers

### Requirement: R-003 — Admin UI

The portal-seguridad MUST provide views to list, create, edit, and toggle active for `tb_numeracion_control` entries. The list MUST show entidad, serie, año, correlativo actual, and active status.

#### Scenario: Create numeración entry from UI
- GIVEN an authenticated user with `numeracion:editar`
- WHEN creating a new numeración entry via the form
- THEN the row is persisted with `correlativo_actual=0`

#### Scenario: Toggle active
- GIVEN an active numeración entry
- WHEN marking it as inactive via UI
- THEN `activo=false`
- AND `nextCorrelativo()` MUST return an error or skip inactive entries
