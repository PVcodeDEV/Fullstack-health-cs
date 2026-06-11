# RRHH CTS Specification

## Purpose

CTS legal calculation per Peruvian Pequeña Empresa REMYPE: semestral deposit (May/November), 15 days per full year, (RC / 360) × días laborados, 1/6 average gratificación in RC, truncamiento 30-day rule, zero deductions. Append-only deposit records per worker per period.

## Requirements

### Requirement: CTS-001 — Semester derivation

The system MUST derive the CTS period from `PeriodoPlanilla.mes`. mes=5 → NOVIEMBRE (semestre: mayo–octubre), mes=11 → MAYO (semestre: noviembre–abril). Any other mes MUST be rejected.

#### Scenario: S-CTS-001-1 — May period

- GIVEN PeriodoPlanilla with mes=5
- WHEN POST `/api/v1/cts/calcular?periodoPlanillaId=X`
- THEN periodo = NOVIEMBRE, semestre = mayo–octubre

#### Scenario: S-CTS-001-2 — Invalid month

- GIVEN PeriodoPlanilla with mes=6
- WHEN POST `/api/v1/cts/calcular`
- THEN returns 400 (invalid semester)

### Requirement: CTS-002 — Días computables

The system MUST count full months as 30 days each (truncamiento). Entry day 1–14 → month counts as 30 days. Entry day ≥15 → month excluded, count starts next month. Active contratos before semestre count all months.

#### Scenario: S-CTS-002-1 — Full semester

- GIVEN contrato.inicio before semestre start
- WHEN calculating días computables for 6-month semester
- THEN díasComputables = 180

#### Scenario: S-CTS-002-2 — Mid-month entry (day ≥15)

- GIVEN contrato.inicio=20/01/2026, semestre=enero–junio
- WHEN calculating días computables
- THEN díasComputables = 150 (5 × 30; January excluded)

#### Scenario: S-CTS-002-3 — Entry day 1–14

- GIVEN contrato.inicio=10/01/2026, semestre=enero–junio
- WHEN calculating días computables
- THEN díasComputables = 180 (January counts as 30 days)

#### Scenario: S-CTS-002-4 — Zero months

- GIVEN contrato.inicio=20/06/2026, semestre=enero–junio
- WHEN calculating días computables
- THEN díasComputables = 0 (entry ≥15 in last month)

### Requirement: CTS-003 — Remuneración computable

RC = sueldo base + asignación familiar (10% RMV if hijos ≥1) + 1/6 promedio of last 2 gratificaciones + 1/6 promedio of last 2 bonif extraordinarias. If <2 gratificaciones, use available. If 0, skip 1/6 addition.

#### Scenario: S-CTS-003-1 — Base only, no hijos, no gratif

- GIVEN sueldoBase=2000, hijos=0, no gratificaciones
- WHEN calculating RC
- THEN RC = 2000

#### Scenario: S-CTS-003-2 — With asignación familiar

- GIVEN sueldoBase=2000, hijos=2, RMV=1130, no gratificaciones
- WHEN calculating RC
- THEN RC = 2000 + 113 = 2113

#### Scenario: S-CTS-003-3 — With average gratificación

- GIVEN sueldoBase=2000, hijos=0, last 2 gratificaciones: 1056 and 1200, bonif: 95 and 108
- WHEN calculating RC
- THEN gratifPromedio = (1056 + 1200) / 2 = 1128; bonifPromedio = (95 + 108) / 2 = 101.50; RC = 2000 + 1128/6 + 101.50/6 = 2204.92

### Requirement: CTS-004 — CTS amount

Pequeña Empresa REMYPE: montoDeposito = (RC / 360) × díasComputables. Zero deductions — no AFP/ONP/Renta/EsSalud applied.

#### Scenario: S-CTS-004-1 — Full semester

- GIVEN RC=2204.92, díasComputables=180
- WHEN calculating CTS
- THEN montoDeposito = (2204.92 / 360) × 180 = 1102.46

#### Scenario: S-CTS-004-2 — Proportional

- GIVEN RC=2000, díasComputables=120
- WHEN calculating CTS
- THEN montoDeposito = (2000 / 360) × 120 = 666.67

#### Scenario: S-CTS-004-3 — Empty period

- GIVEN PeriodoPlanilla with mes=5, no active contratos in semestre
- WHEN POST `/api/v1/cts/calcular`
- THEN returns 200 with empty records list

### Requirement: CTS-005 — Zero deductions

CTS MUST NOT apply AFP/ONP descuento, Renta 5ta, or EsSalud. It is a benefit deposit, not a salary payment. `montoDeposito` equals the full calculated amount.

#### Scenario: S-CTS-005-1 — AFP worker, no deduction

- GIVEN worker with AFP afiliado, montoDeposito=1102.46
- WHEN calculating CTS
- THEN montoDeposito = 1102.46, descuentos=0

### Requirement: CTS-006 — Endpoints and idempotency

`POST /api/v1/cts/calcular?periodoPlanillaId=X` SHALL be idempotent (upserts existing records for same periodo). `GET /api/v1/cts` lists all. `GET /api/v1/cts/{id}` returns one. `@PreAuthorize("hasAuthority('rrhh:ver')")` on GETs, `rrhh:editar` on POST.

#### Scenario: S-CTS-006-1 — First calculation

- GIVEN ABIERTO PeriodoPlanilla May 2026, worker with active contrato
- WHEN POST `/api/v1/cts/calcular?periodoPlanillaId=X`
- THEN returns 201 with deposit records

#### Scenario: S-CTS-006-2 — Idempotent re-run

- GIVEN existing CTS deposit records for May 2026
- WHEN POST `/api/v1/cts/calcular?periodoPlanillaId=X` again
- THEN returns 200 with updated records (no duplicates)

#### Scenario: S-CTS-006-3 — Unauthorized POST

- GIVEN user without `rrhh:editar`
- WHEN POST `/api/v1/cts/calcular`
- THEN returns 403

### Requirement: CTS-007 — PII protection

Monetary fields in `toString()` MUST be excluded (Ley 29733). API responses MUST NOT expose worker PII beyond trabajadorId, nombres, apellidos.

#### Scenario: S-CTS-007-1 — toString excludes monetary fields

- GIVEN DepositoCts entity
- WHEN calling `toString()`
- THEN remuneracionComputable, gratificacionProporcional, montoDeposito are excluded

#### Scenario: S-CTS-007-2 — API minimal worker data

- GIVEN GET `/api/v1/cts/{id}`
- WHEN response returned
- THEN worker fields limited to trabajadorId, nombres, apellidos (no DNI, dirección)
