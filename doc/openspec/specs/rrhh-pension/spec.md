# RRHH Pension Specification

## Purpose

Pension regime foundation for payroll: AFP catalog with historical SBS rates, ONP as special entry without commission structure, and per-worker pension info with CUSPP tracking.

## Requirements

### Requirement: PEN-001 — AFP catalog

The system MUST maintain an AFP catalog (Prima, Profuturo, Habitat, Integra) with `codigo`, `nombre`, `activo`. ONP MUST be stored with `codigo: "ONP"` and null commission fields (`tipoComision`, `tasa`, `primaSeguro`). Expose `GET /api/v1/afps` returning active entries.

#### Scenario: S-PEN-001-1 — GET afps returns active entries only

- GIVEN 4 active AFPs, ONP as active, and 1 inactive AFP
- WHEN GET `/api/v1/afps`
- THEN the response returns 5 entries ordered by codigo, excluding the inactive

#### Scenario: S-PEN-001-2 — ONP entry has null commission fields

- GIVEN ONP is stored in tb_afps
- WHEN GET `/api/v1/afps`
- THEN ONP's `tipoComision`, `tasa`, and `primaSeguro` are null

### Requirement: PEN-002 — Historical rate tracking

The system MUST track AFP rates in `tb_afp_tasas_historicas` with `afpId`, `tipoComision` (FLUJO, MIXTA), `tasa`, `primaSeguro`, `vigenciaDesde`, `vigenciaHasta`. New rates SHALL supersede previous ones by setting `vigenciaHasta` to the day before the new `vigenciaDesde`.

#### Scenario: S-PEN-002-1 — New rate supersedes previous

- GIVEN AFP Prima has an active rate with vigenciaDesde "2026-01-01", vigenciaHasta null
- WHEN a new rate is created with vigenciaDesde "2026-07-01", tasa 1.85
- THEN the previous rate's vigenciaHasta is set to "2026-06-30"
- AND the new rate has vigenciaHasta null

### Requirement: PEN-003 — Worker pension information

The system MUST store one pension info record per Trabajador with `cuspp` (12-char numeric), `afpId`, `comisionTipo` (FLUJO, MIXTA, null), `sctr` (boolean), `fechaAfiliacion`, `estado`. MUST use upsert semantics — first call creates, subsequent calls update.

#### Scenario: S-PEN-003-1 — First call creates new record

- GIVEN a Trabajador exists with no pension info
- WHEN PUT `/api/v1/trabajadores/{id}/informacion-pensionaria` with `{ afpId: "PRIMA", cuspp: "123456789012", comisionTipo: "FLUJO", sctr: false, fechaAfiliacion: "2026-01-01" }`
- THEN the system returns 200 with the created record
- AND `estado` is `ACTIVO`

#### Scenario: S-PEN-003-2 — Second call updates existing record

- GIVEN a Trabajador has pension info referencing AFP Prima
- WHEN PUT with `{ afpId: "HABITAT", ... }` for the same Trabajador
- THEN the existing record's `afpId` is updated
- AND no duplicate record is created

#### Scenario: S-PEN-003-3 — Invalid CUSPP rejected

- GIVEN a valid Trabajador
- WHEN PUT with `cuspp: "12345"` (5 chars)
- THEN the system returns 409: "CUSPP debe tener 12 dígitos"

### Requirement: PEN-004 — ONP special handling

When `afpId` is ONP, `comisionTipo` MUST be nullable, CUSPP MUST NOT be required, `sctr` MUST be nullable. The `cuspp` field SHALL be auto-populated with the Trabajador's DNI.

#### Scenario: S-PEN-004-1 — ONP pension info accepts null comision

- GIVEN a Trabajador with DNI "12345678"
- WHEN PUT with `{ afpId: "ONP", comisionTipo: null, sctr: null, fechaAfiliacion: "2026-01-01" }`
- THEN the system returns 200
- AND `comisionTipo` is null
- AND `cuspp` is auto-populated with "12345678"

### Requirement: PEN-005 — Endpoints

The system MUST expose `GET/PUT /api/v1/trabajadores/{id}/informacion-pensionaria` and `GET /api/v1/afps`. Enforce `@PreAuthorize("hasAuthority('rrhh:ver')")` on reads, `rrhh:editar` on writes.

#### Scenario: S-PEN-005-1 — GET returns pension info

- GIVEN a Trabajador with pension info exists
- WHEN GET `/api/v1/trabajadores/{id}/informacion-pensionaria`
- THEN returns 200 with afpId, comisionTipo, sctr, cuspp, fechaAfiliacion, estado

#### Scenario: S-PEN-005-2 — Unauthorized PUT returns 403

- GIVEN a user without `rrhh:editar`
- WHEN PUT `/api/v1/trabajadores/{id}/informacion-pensionaria`
- THEN the response returns 403 Forbidden

### Requirement: PEN-006 — PII protection

The system MUST exclude `cuspp` from `toString()`, logs, and serialization via `@ToString.Exclude`. Error messages and audit logs MUST NOT contain the full CUSPP value.

#### Scenario: S-PEN-006-1 — CUSPP excluded from toString

- GIVEN a pension info entity with cuspp "123456789012"
- WHEN `toString()` is called
- THEN the output DOES NOT contain "123456789012"
