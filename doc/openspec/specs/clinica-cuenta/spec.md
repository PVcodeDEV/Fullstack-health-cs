# Clinica Cuenta Specification

## Purpose

Patient account tracking: surgical package assignment, additional charge tracking (extra days, room changes, extra supplies), and integration boundary with future Caja module. Lives in clinica temporarily with documented extraction plan.

## Requirements

### Requirement: CTA-001 — Account creation from admission

The system MUST create a `Cuenta` record when Admisión processes an admission. The account SHALL include `pacienteId`, `paqueteQuirurgicoId`, `tipoHabitacionId`, `fechaApertura`, `estado` (ABIERTO/CERRADO), and `totalEstimado` (nullable, informational only). Price SHALL NOT be visible to Recepción/Admisión roles.

#### Scenario: CTA-001-1 — Account created on admission

- GIVEN a Paciente admitted via Admisión
- WHEN the admission flow completes
- THEN a Cuenta is created with `estado: ABIERTO`, `fechaApertura` set server-side

#### Scenario: CTA-001-2 — Admisión cannot see prices

- GIVEN a Cuenta with `totalEstimado: 5000.00`
- WHEN a Usuario with role RECEPCION or ADMISION queries the account
- THEN `totalEstimado` is returned as null or excluded from the response

### Requirement: CTA-002 — Additional charges during stay

The system MUST allow registering additional charges during hospitalization: `CARGO_TIPO` (DIAS_EXTRA, CAMBIO_HABITACION, INSUMOS, OTROS), `monto`, `descripcion`, `fechaRegistro`, `usuarioId`. Charges SHALL accumulate toward the account total.

#### Scenario: CTA-002-1 — Register extra day charge

- GIVEN an active Cuenta linked to a Hospitalizacion that exceeded expected stay
- WHEN POST `/api/v1/clinica/cuenta/{id}/cargos` with `{ tipo: DIAS_EXTRA, monto: 250.00, descripcion: "Día extra 2026-06-05" }`
- THEN the charge is registered with 201
- AND the account `totalCargos` is updated to include 250.00

#### Scenario: CTA-002-2 — Charge on closed account

- GIVEN a Cuenta with `estado: CERRADO`
- WHEN POST a new charge
- THEN the system returns 422 with "account already closed"

### Requirement: CTA-003 — Account closure and Caja boundary

The system MUST allow marking account as ready for Caja (`estado: PENDIENTE_COBRO`) when clinical discharge is registered. The final closure (`CERRADO`) SHALL only happen when Caja confirms payment. This is an explicit extraction boundary — documenting where the Caja module will take over.

#### Scenario: CTA-003-1 — Pendiente cobro on alta

- GIVEN a Cuenta linked to a Hospitalizacion that received clinical discharge
- WHEN the alta is registered
- THEN the Cuenta `estado` changes to `PENDIENTE_COBRO`
- AND a flag `pendienteCobro` is set to true

#### Scenario: CTA-003-2 — Bed release on cobro confirmation

- GIVEN a Cuenta with `estado: PENDIENTE_COBRO`
- WHEN an endpoint `PUT /api/v1/clinica/cuenta/{id}/confirmar-cobro` is called (by Caja or ADMIN)
- THEN `estado` becomes `CERRADO`, the bed releases to `DISPONIBLE`
- AND the Hospitalizacion `estado` changes to `FINALIZADO`

### Requirement: CTA-004 — Account query with charges summary

The system MUST support querying an account with full charge details. The response SHALL include `cuenta` header info, a `cargos[]` array, and computed `totalCargos`.

#### Scenario: CTA-004-1 — View account detail

- GIVEN a Cuenta with 3 additional charges
- WHEN GET `/api/v1/clinica/cuenta/{id}`
- THEN the response includes the account header, all 3 charges, and `totalCargos`

### Requirement: CTA-005 — Extraction boundary documentation

The codebase MUST document the Caja extraction plan. A `package-info.java` in `com.clinica.clinica.cuenta` SHALL contain the note: "Extraction target: com.clinica.caja.cuenta — move when Caja module is built." The `Cuenta` entity SHALL NOT reference any `caja` package — only clinica-internal types.

#### Scenario: CTA-005-1 — No caja dependency

- GIVEN the `clinica.cuenta` source tree
- WHEN scanning imports
- THEN NO import from `com.clinica.caja` exists

### Requirement: CTA-006 — Permission granularity

Endpoints MUST use `cuenta:{accion}` where `accion` is `crear`, `ver`, `editar`, `cargar`, `confirmar_cobro`. `cuenta:confirmar_cobro` SHALL be restricted to CAJA and ADMIN roles.

#### Scenario: CTA-006-1 — Enfermeria cannot confirm cobro

- GIVEN a Usuario with role ENFERMERIA
- WHEN PUT to confirm cobro
- THEN the system returns 403 Forbidden

### Requirement: CTA-007 — Data privacy

Account charges and amounts MUST NOT appear in system logs. `monto` and `totalCargos` fields SHALL be annotated with `@ToString.Exclude`. List endpoints MUST NOT expose charge details — only account header.
