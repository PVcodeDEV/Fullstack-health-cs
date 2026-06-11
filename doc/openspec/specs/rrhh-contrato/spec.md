# RRHH Contrato Specification

## Purpose

Contrato lifecycle management for workers: typed contracts with estado state machine, remuneration, multiple-contrato rules, and auto-expiration logic.

## Requirements

### Requirement: CON-001 — Tipo contrato

The system MUST support `tipo` values INDETERMINADO, DETERMINADO, CAS, LOCACION, TIEMPO_PARCIAL, and INTERMITENTE. The `tipo` SHALL reference a `TipoContrato` catalog entry in the maestro module.

#### Scenario: S-CON-001-1 — Create INDETERMINADO without fechaFin

- GIVEN a Trabajador exists
- WHEN POST `/api/v1/trabajadores/{id}/contratos` with `{ tipo: "INDETERMINADO", fechaInicio: "2026-01-01" }`
- THEN the system returns 201
- AND `fechaFin` is null

### Requirement: CON-002 — Fechas and periodo prueba

The system MUST require `fechaInicio` (NOT NULL). `fechaFin` SHALL be optional except when `tipo` is DETERMINADO — then it MUST be provided. `periodoPrueba` SHALL be an integer in months (nullable).

#### Scenario: S-CON-002-1 — Create DETERMINADO fails without fechaFin

- GIVEN a Trabajador exists
- WHEN POST with `{ tipo: "DETERMINADO", fechaInicio: "2026-01-01" }` and no `fechaFin`
- THEN the system returns 422: "fechaFin is required for DETERMINADO contracts"

### Requirement: CON-003 — Remuneracion and jornada

The system MUST store `remuneracion` (BigDecimal, NOT NULL) and `jornada` (REGULAR, PARCIAL, NOCTURNA enum, NOT NULL).

### Requirement: CON-004 — Estado state machine

The system MUST enforce the following estado transitions:

- `CREADO` → `ACTIVO` (on first activation)
- `ACTIVO` → `SUSPENDIDO` (suspend)
- `SUSPENDIDO` → `ACTIVO` (reactivar)
- `ACTIVO` → `VENCIDO` (by date, automatic)
- `ACTIVO` or `SUSPENDIDO` → `RESUELTO` (terminal — no transition out)

No transition from `RESUELTO` SHALL be allowed. `VENCIDO` SHALL be terminal except for renewal.

#### Scenario: S-CON-004-1 — Resolver ACTIVE → RESUELTO succeeds

- GIVEN a Contrato with `estado: "ACTIVO"`
- WHEN PUT `/api/v1/contratos/{id}/resolver` with `{ motivoCese: "Renuncia voluntaria" }`
- THEN the system returns 200 with `estado: "RESUELTO"`
- AND subsequent state transitions return 409

#### Scenario: S-CON-004-2 — Reactivar RESUELTO fails

- GIVEN a Contrato with `estado: "RESUELTO"`
- WHEN PUT `/api/v1/contratos/{id}/reactivar`
- THEN the system returns 409 Conflict with error: "Cannot reactivar a RESUELTO contract"

### Requirement: CON-005 — Single active contract per trabajador

A Trabajador MAY have multiple contratos, but only one SHALL be `ACTIVO` at any time. Creating a new Contrato with `ACTIVO` estado MUST auto-set the previous `ACTIVO` contrato to `VENCIDO`.

#### Scenario: S-CON-005-1 — New ACTIVE auto-expires previous

- GIVEN a Trabajador has one Contrato with `estado: "ACTIVO"`
- WHEN POST `/api/v1/trabajadores/{id}/contratos` with a new ACTIVE contrato
- THEN the previous contrato's estado becomes `VENCIDO`
- AND the new contrato is `ACTIVO`
- AND the response returns 201

### Requirement: CON-006 — Document reference

The system MUST store `contratoEscaneadoId` as a nullable FK column referencing a future document entity. The column SHALL exist in the database but no FK constraint SHALL be enforced until the document module exists.

### Requirement: CON-007 — Endpoints

The system MUST expose:

- `GET /api/v1/trabajadores/{id}/contratos` — list, ordered by `fechaInicio` DESC
- `POST /api/v1/trabajadores/{id}/contratos` — create
- `GET /api/v1/contratos/{id}` — get by ID
- `PUT /api/v1/contratos/{id}` — update
- `PUT /api/v1/contratos/{id}/resolver` — resolve terminal
- `PUT /api/v1/contratos/{id}/reactivar` — reactivate (SUSPENDIDO only)
- `PUT /api/v1/contratos/{id}/suspender` — suspend

#### Scenario: S-CON-007-1 — GET contratos returns ordered list

- GIVEN a Trabajador with 3 contratos (2024, 2025, 2026)
- WHEN GET `/api/v1/trabajadores/{id}/contratos`
- THEN the response returns contratos ordered by `fechaInicio` desc (2026 first, 2024 last)

### Requirement: CON-008 — PII protection

Contract remuneration (`remuneracion`) MUST NOT appear in logs or `toString()`. Use `@ToString.Exclude`.
