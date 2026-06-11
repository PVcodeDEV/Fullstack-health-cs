# RRHH Gratificación Specification

## Purpose

Gratificación legal per Peruvian Pequeña Empresa REMYPE: ½-month salary in July and December, proportional for <6 months, 9% extraordinary bonus (Ley 30334), zero deductions. Append-only records per worker per semester.

## Requirements

### Requirement: GRT-001 — Semester calculation

The system MUST derive the semester from `PeriodoPlanilla.mes`. mes=6 → Enero-Junio, mes=12 → Julio-Diciembre. Any other mes MUST be rejected.

#### Scenario: S-GRT-001-1 — June period

- GIVEN PeriodoPlanilla with mes=6
- WHEN POST `/api/v1/gratificaciones/calcular?periodoPlanillaId=X`
- THEN semester = "Enero-Junio"

#### Scenario: S-GRT-001-2 — Invalid month

- GIVEN PeriodoPlanilla with mes=3
- WHEN POST `/api/v1/gratificaciones/calcular`
- THEN returns 400 (invalid semester)

### Requirement: GRT-002 — Months computables

The system MUST count full months worked: inicio day 1-14 → month counts, day 15+ → month starts next. Contratos active before semester count all 6 months; those starting during semester count from the applicable month.

#### Scenario: S-GRT-002-1 — Full semester

- GIVEN contrato.inicio=10/01/2026, semester=Enero-Junio
- WHEN calculating meses computables
- THEN mesesComputables=6

#### Scenario: S-GRT-002-2 — Mid-month start

- GIVEN contrato.inicio=20/01/2026, semester=Enero-Junio
- WHEN calculating meses computables
- THEN mesesComputables=5 (Febrero-Junio; January excluded)

### Requirement: GRT-003 — Gratificación calculation

Pequeña Empresa REMYPE: 6 meses → ½ × remuneracionComputable. <6 meses → (remuneracionComputable / 12) × mesesComputables. RemuneracionComputable = sueldo base + asignación familiar (if ≥1 hijo).

#### Scenario: S-GRT-003-1 — Full semester

- GIVEN meses=6, sueldoBase=2000, asignacionFamiliar=113
- WHEN calculating gratificación
- THEN gratificación = (2000+113) / 2 = 1056.50

#### Scenario: S-GRT-003-2 — Proportional

- GIVEN meses=5, sueldoBase=2000, asignacionFamiliar=0
- WHEN calculating gratificación
- THEN gratificación = (2000 / 12) × 5 = 833.33

### Requirement: GRT-004 — Bonificación Extraordinaria

The system MUST apply 9% of gratificación as employer APORTE (Ley 30334). It MUST NOT be deducted from worker pay.

#### Scenario: S-GRT-004-1 — Bonus on full gratificación

- GIVEN gratificación=1056.50
- WHEN calculating bonifExtraordinaria
- THEN bonifExtraordinaria = 1056.50 × 0.09 = 95.09
- AND total (costo empleador) = 1056.50 + 95.09 = 1151.59
- Worker receives gratificación (1056.50); bonif is employer APORTE, not deducted

### Requirement: GRT-005 — Zero deductions

Gratificación MUST NOT apply AFP/ONP descuento, Renta 5ta, EsSalud, or any other deduction. Worker receives gratificación only. `total` in the entity represents total employer cost (gratificación + bonif extraordinaria), not worker take-home.

#### Scenario: S-GRT-005-1 — AFP worker, no deduction

- GIVEN worker with AFP afiliado, gratificación=1056.50, bonifExtraordinaria=95.09
- WHEN calculating worker total
- THEN gratificación (trabajador) = 1056.50, total (costo empleador) = 1151.59, descuentos=0

### Requirement: GRT-006 — Endpoints and idempotency

`POST /api/v1/gratificaciones/calcular?periodoPlanillaId=X` SHALL be idempotent (upserts existing records for same semester). `GET /api/v1/gratificaciones` lists all. `GET /api/v1/gratificaciones/{id}` returns one. `@PreAuthorize("hasAuthority('rrhh:ver')")` on GETs, `rrhh:editar` on POST.

#### Scenario: S-GRT-006-1 — First calculation

- GIVEN ABIERTO PeriodoPlanilla June 2026, worker with active contrato
- WHEN POST `/api/v1/gratificaciones/calcular?periodoPlanillaId=X`
- THEN returns 201 with gratificación records

#### Scenario: S-GRT-006-2 — Idempotent re-run

- GIVEN existing gratificación records for June 2026
- WHEN POST `/api/v1/gratificaciones/calcular?periodoPlanillaId=X` again
- THEN returns 200 with updated records (no duplicates)

#### Scenario: S-GRT-006-3 — Unauthorized POST

- GIVEN user without `rrhh:editar`
- WHEN POST `/api/v1/gratificaciones/calcular`
- THEN returns 403

### Requirement: GRT-007 — PII protection

Monetary fields in `toString()` MUST be excluded (Ley 29733). API responses MUST NOT expose worker PII beyond trabajadorId, nombres, apellidos.

#### Scenario: S-GRT-007-1 — toString excludes monetary fields

- GIVEN Gratificacion entity
- WHEN calling `toString()`
- THEN gratificacion, bonifExtraordinaria, total are excluded

#### Scenario: S-GRT-007-2 — API minimal worker data

- GIVEN GET `/api/v1/gratificaciones/{id}`
- WHEN response returned
- THEN worker fields limited to id, nombres, apellidos (no DNI, dirección)
