# RRHH Derechohabiente Specification

## Purpose

Beneficiary (derechohabiente) tracking for workers: links a Persona to a Trabajador as a dependent, with relation type, automatic coverage dates for children, and cascading deactivation on contract resolution.

## Requirements

### Requirement: DER-001 — Core fields

The system MUST store `trabajadorId` (FK to Trabajador), `personaId` (FK to Persona), `relacion` (CONYUGE, HIJO, CONCUBINO, PADRE, MADRE enum), `fechaInicio`, `fechaFin`, and `estado` (ACTIVO, INACTIVO). `trabajadorId`, `personaId`, `relacion`, `fechaInicio`, and `estado` MUST be NOT NULL.

#### Scenario: S-DER-001-1 — Create HIJO with existing Persona

- GIVEN a Trabajador and a Persona with `fechaNacimiento` exist
- WHEN POST `/api/v1/trabajadores/{id}/derechohabientes` with `{ personaId, relacion: "HIJO", fechaInicio: "2026-01-01" }`
- THEN the system returns 201 with `estado: "ACTIVO"`

#### Scenario: S-DER-001-2 — Create with non-existent Persona fails

- GIVEN a `personaId` that does not exist
- WHEN POST `/api/v1/trabajadores/{id}/derechohabientes`
- THEN the system returns 404 with EntityNotFoundException

### Requirement: DER-002 — Multiple trabajadores

A single Persona MAY be derechohabiente of multiple Trabajadores simultaneously (e.g., both parents work at the clinic). The system MUST NOT enforce uniqueness on the `personaId + trabajadorId` pair beyond one active record per pair.

### Requirement: DER-003 — Persona reference

The system MUST reference the existing `Persona` entity directly. The Persona MAY already exist in another role (patient, worker, etc.). Derechohabiente does NOT create a new Persona — it only links an existing one.

### Requirement: DER-004 — Child age auto-calculation

When `relacion` is `HIJO`, the system MUST automatically set `fechaFin = fechaInicio + 18 years` (using `Persona.fechaNacimiento`). The system SHOULD flag derechohabientes when the child approaches 18 (within 90 days of `fechaFin`).

#### Scenario: S-DER-004-1 — HIJO auto-calculates fechaFin

- GIVEN a Persona with `fechaNacimiento: "2010-06-01"`
- WHEN POST a derechohabiente with `relacion: "HIJO"` and `fechaInicio: "2026-06-01"`
- THEN the system sets `fechaFin: "2044-06-01"` (fechaInicio + 18 years)

### Requirement: DER-005 — Auto-inactivation on contrato resolution

When a Contrato transitions to `RESUELTO`, the system MUST automatically set `estado: "INACTIVO"` for all derechohabientes linked to that Trabajador. The transition MUST happen in the same transaction.

#### Scenario: S-DER-005-1 — Contrato RESUELTO cascades to derechohabientes

- GIVEN a Trabajador with 2 active derechohabientes
- WHEN PUT `/api/v1/contratos/{id}/resolver`
- THEN the contrato estado becomes `RESUELTO`
- AND both derechohabientes transition to `INACTIVO` in the same transaction

### Requirement: DER-006 — Manual deactivation

The system MUST allow setting a derechohabiente to `INACTIVO` without affecting the associated contrato. The contrato SHALL remain in its current estado.

#### Scenario: S-DER-006-1 — Manual INACTIVO keeps contrato ACTIVE

- GIVEN a derechohabiente with `estado: "ACTIVO"` and its contrato with `estado: "ACTIVO"`
- WHEN PUT `/api/v1/derechohabientes/{id}` with `{ estado: "INACTIVO" }`
- THEN the derechohabiente becomes `INACTIVO`
- AND the contrato remains `ACTIVO`

### Requirement: DER-007 — Endpoints

The system MUST expose:

- `GET /api/v1/trabajadores/{id}/derechohabientes` — list active by default
- `POST /api/v1/trabajadores/{id}/derechohabientes` — create
- `GET /api/v1/derechohabientes/{id}` — get by ID
- `PUT /api/v1/derechohabientes/{id}` — update (including estado)
- `GET /api/v1/trabajadores/{id}/derechohabientes?estado=INACTIVO` — filter by estado

#### Scenario: S-DER-007-1 — GET active derechohabientes

- GIVEN a Trabajador with 2 ACTIVE and 1 INACTIVE derechohabiente
- WHEN GET `/api/v1/trabajadores/{id}/derechohabientes`
- THEN the response returns only the 2 ACTIVE records

### Requirement: DER-008 — PII protection

Derechohabiente endpoint responses MUST NOT expose `Persona.numeroDocumento`, `Persona.nombres`, `Persona.apellidos`, `Persona.direccion`, `Persona.telefono`, or `Persona.email` in logs. Use `@ToString.Exclude`.
