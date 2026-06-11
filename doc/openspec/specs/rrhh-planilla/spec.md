# RRHH Planilla Specification

## Purpose

Monthly payroll: period lifecycle, concept catalog, worker liquidation with AFP/ONP/EsSalud/Renta 5ta, append-only audit trail. RMV/UIT via `app.rrhh` in `application.yml`.

## Requirements

### Requirement: PLN-001 — Period lifecycle

The system MUST support monthly periods with `estado` ABIERTO, CERRADO, ANULADO. Only one ABIERTO period at a time. Once CERRADO, no edits or regeneration.

#### Scenario: S-PLN-001-1 — Duplicate ABIERTO rejected

- GIVEN no ABIERTO period for 2026-ENERO
- WHEN POST `/api/v1/periodos-planilla` with `{ anio: 2026, mes: "ENERO" }`
- THEN returns 201; second POST with same body returns 409

#### Scenario: S-PLN-001-2 — Close blocks regeneration

- GIVEN ABIERTO period with generated planilla
- WHEN PUT `/api/v1/periodos-planilla/{id}/cerrar`
- THEN returns 200 `estado: "CERRADO"`; POST generar for this period returns 409

### Requirement: PLN-002 — Concept catalog

The system MUST maintain concepts with `codigo`, `nombre`, `tipo` (INGRESO, DESCUENTO, APORTE), `activo`. Seven seeded via Flyway V24: BASICO, ASIGNACION_FAMILIAR, AFP_OBLIGATORIO, ONP_DESCUENTO, ESSALUD_APORTE, RENTA_5TA, ADELANTO.

#### Scenario: S-PLN-002-1 — GET returns seeded values

- GIVEN Flyway V24 executed
- WHEN GET `/api/v1/conceptos-planilla`
- THEN returns 200 with 7 concepts, each with codigo, nombre, tipo

### Requirement: PLN-003 — Payroll generation

For each worker with ACTIVE contrato in ABIERTO period: sueldo base (prorated), asignación familiar (10% RMV if hijos >= 1), AFP/ONP descuento, Renta 5ta, EsSalud aporte. Neto = ingresos - descuentos.

#### Scenario: S-PLN-003-1 — Generate with active workers

- GIVEN ABIERTO period and 2 workers with ACTIVE contratos (AFP + ONP)
- WHEN POST `/api/v1/planillas/generar` with `{ periodoPlanillaId }`
- THEN returns 201 with header and 2 detalle lines

#### Scenario: S-PLN-003-2 — No active contracts

- GIVEN ABIERTO period with zero active contratos
- WHEN POST `/api/v1/planillas/generar`
- THEN returns 200 with empty detalle list, `totalNeto: 0.00`

### Requirement: PLN-004 — AFP/ONP deduction

AFP: descuento = remuneracionComputable × (tasa + primaSeguro) from `tb_afp_tasas_historicas`. ONP: descuento = remuneracionComputable × 13%.

#### Scenario: S-PLN-004-1 — AFP rate applied

- GIVEN AFP worker, remuneracionComputable = 2500, AFP Prima tasa=1.85%, prima=0.87%
- WHEN planilla generates
- THEN AFP descuento = 2500 × 0.0272 = 68.00

#### Scenario: S-PLN-004-2 — ONP flat 13%

- GIVEN ONP worker, remuneracionComputable = 2500
- WHEN planilla generates
- THEN ONP descuento = 2500 × 0.13 = 325.00

### Requirement: PLN-005 — EsSalud contribution

EsSalud = 9% of remuneracionComputable. Employer APORTE, not a worker deduction. In totalAportes, not neto.

#### Scenario: S-PLN-005-1 — EsSalud as aporte only

- GIVEN worker, remuneracionComputable = 2500
- WHEN planilla generates
- THEN EsSalud = 225.00 in totalAportes; neto unaffected

### Requirement: PLN-006 — Renta 5ta Categoría

Project annual income (remuneracion × 12 + acumulado), deduct 7 UIT, apply brackets (8% up to 5 UIT, 14% up to 20 UIT, 17% up to 35 UIT, 20% up to 45 UIT, 30% excess). Monthly = (annual tax / 12) - already withheld.

#### Scenario: S-PLN-006-1 — Below 7 UIT

- GIVEN worker, remuneracion = 1500, UIT = 5350 (projected 18000 < 37450)
- WHEN planilla generates
- THEN Renta 5ta = 0.00

#### Scenario: S-PLN-006-2 — Bracket applied

- GIVEN worker, remuneracion = 10000, UIT = 5350, no prior accumulation
- WHEN planilla generates
- THEN projected 120000, renta neta = 82550, withholding > 0 on (82550 / 12)

### Requirement: PLN-007 — Payroll lines

Each payroll MUST have one PlanillaDetalle per worker with sueldoBase, asignacionFamiliar, totals, and JSON concept breakdown.

#### Scenario: S-PLN-007-1 — Full breakdown

- GIVEN generated planilla for one worker
- WHEN GET `/api/v1/planillas/{id}`
- THEN each detalle includes monetary fields and conceptos map with per-concept amounts

### Requirement: PLN-008 — Endpoints and security

`GET/POST /api/v1/periodos-planilla`, `PUT /{id}/cerrar`; `GET /api/v1/planillas`, `GET /{id}`, `POST /generar`; `GET /api/v1/conceptos-planilla` (public). `@PreAuthorize("hasAuthority('rrhh:ver')")` on reads, `rrhh:editar` on writes.

#### Scenario: S-PLN-008-1 — Authorized read

- GIVEN user with `rrhh:ver` and generated planilla
- WHEN GET `/api/v1/planillas`
- THEN returns 200

#### Scenario: S-PLN-008-2 — Unauthorized write

- GIVEN user without `rrhh:editar`
- WHEN POST `/api/v1/planillas/generar`
- THEN returns 403
