# RRHH Vacación Specification

## Purpose

Pequeña Empresa REMYPE vacation tracking: 15-day annual descanso after 12-month record period, programar/iniciar/completar lifecycle, proportional reduction for inasistencias, loss at expiry. Two entities — VacacionRecord (derecho adquirido) and VacacionGoce (effective descanso).

## Requirements

### Requirement: VAC-001 — Record at 12-month anniversary

The system MUST create a VacacionRecord when a worker completes 12 continuous months under an active Contrato. Captures periodoRecord, diasCorrespondientes=15, estado=PENDIENTE.

#### Scenario: S-VAC-001-1 — Full year completed

- GIVEN Contrato inicio=01/01/2026, active through 31/12/2026
- WHEN anniversary evaluated on 01/01/2027
- THEN VacacionRecord created: fechaInicio=01/01/2026, fechaFin=31/12/2026, diasCorrespondientes=15, estado=PENDIENTE

#### Scenario: S-VAC-001-2 — Sub-12-month no record

- GIVEN Contrato inicio=01/06/2026, current date=01/03/2027
- WHEN anniversary evaluated
- THEN no VacacionRecord created (11 months, incomplete)

### Requirement: VAC-002 — Goce lifecycle

The system MUST support VacacionGoce states: PROGRAMADO → EN_CURSO → COMPLETADO. Multiple Goces per Record allowed (fractional goce), sum ≤ diasCorrespondientes. Minimum goce: 7 days. Each goce captures fechaInicio, fechaFin, diasEfectivos, remuneracionVacacional.

#### Scenario: S-VAC-002-1 — Full 15-day goce

- GIVEN VacacionRecord with 15 diasCorrespondientes, estado=PENDIENTE
- WHEN full goce programmed → started → completed
- THEN Goce lifecycle: PROGRAMADO → EN_CURSO → COMPLETADO; record estado → COMPLETADO

#### Scenario: S-VAC-002-2 — Fractional split (8+7)

- GIVEN VacacionRecord with 15 diasCorrespondientes
- WHEN first goce=8d, second goce=7d
- THEN two Goces created; sum=15=diasCorrespondientes; record COMPLETADO after second

#### Scenario: S-VAC-002-3 — Exceeds available

- GIVEN VacacionRecord with 10 diasPendientes
- WHEN goce programmed for 12 days
- THEN returns 400 (exceeds available balance)

#### Scenario: S-VAC-002-4 — Below 7-day minimum

- GIVEN VacacionRecord with 15 days available
- WHEN goce programmed for 5 days
- THEN returns 400 (minimum 7 days per period)

#### Scenario: S-VAC-002-5 — Invalid transition

- GIVEN VacacionGoce estado=EN_CURSO
- WHEN POST `/api/v1/vacaciones/goces/{id}/iniciar` (re-initiate)
- THEN returns 400 (invalid state transition)

### Requirement: VAC-003 — Remuneración

The vacation remuneration MUST be the worker's current sueldo + asignación familiar at descanso start. Captured in VacacionGoce.remuneracionVacacional at PROGRAMADO creation.

#### Scenario: S-VAC-003-1 — Captured at programming

- GIVEN Contrato sueldo=2500, asignacionFamiliar=113
- WHEN VacacionGoce PROGRAMADO
- THEN remuneracionVacacional = 2613

#### Scenario: S-VAC-003-2 — Post-programming raise unaffected

- GIVEN VacacionGoce PROGRAMADO, remuneracion=2613
- WHEN worker raises to 3000 before goce starts
- THEN VacacionGoce.remuneracionVacacional remains 2613

### Requirement: VAC-004 — Reduction for inasistencias

Each full calendar month of unjustified absence or unpaid leave within the record period SHALL reduce diasCorrespondientes by 1/12 (1.25d). Partial months ignored. Reduction at record creation.

#### Scenario: S-VAC-004-1 — One full month

- GIVEN 1 full month inasistencia injustificada in record period
- WHEN VacacionRecord created
- THEN diasCorrespondientes = 15 − 1.25 = 13.75

#### Scenario: S-VAC-004-2 — 12 months (derecho extinguished)

- GIVEN 12 months of licencia sin goce in record period
- WHEN VacacionRecord created
- THEN diasCorrespondientes = 0; no record created

#### Scenario: S-VAC-004-3 — Partial month ignored

- GIVEN 15 days inasistencia (not a full calendar month)
- WHEN VacacionRecord created
- THEN diasCorrespondientes = 15 (no reduction)

### Requirement: VAC-005 — Loss at expiry

If no VacacionGoce completed within 12 months after record.fechaFinRecord, the derecho SHALL be marked PERDIDO. Indemnización is out of scope.

#### Scenario: S-VAC-005-1 — Auto-loss

- GIVEN VacacionRecord PENDIENTE, fechaFinRecord=31/12/2026
- WHEN 31/12/2027 passes with no COMPLETADO goce
- THEN record.estado → PERDIDO

#### Scenario: S-VAC-005-2 — Goce within window prevents loss

- GIVEN VacacionRecord, fechaFinRecord=31/12/2026
- WHEN goce completed 15/11/2027
- THEN record.estado = COMPLETADO (not PERDIDO)

### Requirement: VAC-006 — Endpoints and idempotency

POST registrar-record SHALL be idempotent (find-or-create). POST programar, iniciar, completar transition VacacionGoce states. GET endpoints filterable by trabajadorId. `@PreAuthorize("hasAuthority('rrhh:ver')")` on GETs, `rrhh:editar` on writes.

#### Scenario: S-VAC-006-1 — First record creation

- GIVEN worker with 12-month anniversary
- WHEN POST `/api/v1/vacaciones/registrar?trabajadorId=X`
- THEN returns 201 with VacacionRecord

#### Scenario: S-VAC-006-2 — Idempotent re-run

- GIVEN existing VacacionRecord for same worker+period
- WHEN POST `/api/v1/vacaciones/registrar` again
- THEN returns 200 (existing record, no duplicate)

#### Scenario: S-VAC-006-3 — Unauthorized write

- GIVEN user without `rrhh:editar`
- WHEN POST any vacacion write endpoint
- THEN returns 403

#### Scenario: S-VAC-006-4 — GET by worker

- GIVEN records and goces exist for trabajadorId=5
- WHEN GET `/api/v1/vacaciones/records?trabajadorId=5`
- THEN returns 200 with filtered list

### Requirement: VAC-007 — PII protection

Monetary and personal data MUST be excluded from `toString()`. API responses MUST NOT expose PII beyond trabajadorId, nombres, apellidos (Ley 29733).

#### Scenario: S-VAC-007-1 — toString excludes fields

- GIVEN VacacionRecord and VacacionGoce entities
- WHEN calling `toString()`
- THEN remuneracionVacacional, diasCorrespondientes excluded

#### Scenario: S-VAC-007-2 — API worker data minimized

- GIVEN GET `/api/v1/vacaciones/records/{id}`
- WHEN response returned
- THEN worker fields: solo trabajadorId, nombres, apellidos (no DNI, dirección)
