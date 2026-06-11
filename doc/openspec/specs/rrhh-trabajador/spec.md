# RRHH Trabajador Specification

## Purpose

Expanded Trabajador entity for Peruvian labor law compliance (REMYPE, regimen laboral, colegiatura, carga familiar). Backward-compatible CMP migration from Medico entity. Sub-resource endpoints for contratos and derechohabientes.

## Requirements

### Requirement: TRA-001 — Tipo and regimen laboral

The system MUST store `tipo` (MEDICO, ENFERMERA, NUTRICIONISTA, TECNICO, ADMINISTRATIVO, OTROS), `regimenLaboral` (PRIVADO, CAS, LOCACION, TERCEROS), and REMYPE fields (`fechaIngreso`, `fechaCese`, `motivoCese`). All fields except `fechaCese` and `motivoCese` MUST be NOT NULL.

#### Scenario: S-TRA-001-1 — Create MEDICO with all fields

- GIVEN a Persona exists
- WHEN POST `/api/v1/trabajadores` with `{ tipo: "MEDICO", regimenLaboral: "PRIVADO", fechaIngreso: "2026-01-15", cargo: "Cirujano General" }`
- THEN the system returns 201 with the Trabajador record
- AND `tipo`, `regimenLaboral`, `fechaIngreso` are persisted

### Requirement: TRA-002 — Bank information

The system MAY store `banco`, `cuentaSueldo`, and `CCI` on the Trabajador. All three fields SHALL be nullable. No bank fields are required for creation.

#### Scenario: S-TRA-002-1 — Create without bank info

- GIVEN a valid Trabajador request without `banco`, `cuentaSueldo`, `CCI`
- WHEN POST `/api/v1/trabajadores`
- THEN the system returns 201 with bank fields null

### Requirement: TRA-003 — Emergency contact

The system MAY store `contactoEmergenciaNombre` and `contactoEmergenciaTelefono`. Both SHALL be nullable.

### Requirement: TRA-004 — Carga familiar

The system MUST store `cantidadHijos` as an integer defaulting to 0. The field SHALL accept values 0 or greater.

### Requirement: TRA-005 — Colegiatura validation

The system MUST store `nroColegiatura` and `tipoColegiatura` (CMP, CEP, CPN, OTROS). Both SHALL be nullable. The system MUST validate: if `tipo` is MEDICO, ENFERMERA, or NUTRICIONISTA, then `tipoColegiatura` and `nroColegiatura` are required. Validation applies on both CREATE and UPDATE.

#### Scenario: S-TRA-005-1 — Create MEDICO with CMP succeeds

- GIVEN a Persona exists and `tipo: "MEDICO"`
- WHEN POST `/api/v1/trabajadores` with `{ tipo: "MEDICO", tipoColegiatura: "CMP", nroColegiatura: "12345" }`
- THEN the system returns 201

#### Scenario: S-TRA-005-2 — Create MEDICO without CMP fails

- GIVEN a Persona exists and `tipo: "MEDICO"`
- WHEN POST `/api/v1/trabajadores` without `tipoColegiatura` or `nroColegiatura`
- THEN the system returns 422 with validation error: "colegiatura required for MEDICO"

#### Scenario: S-TRA-005-3 — Create ADMINISTRATIVO without colegiatura succeeds

- GIVEN a Persona exists and `tipo: "ADMINISTRATIVO"`
- WHEN POST `/api/v1/trabajadores` without colegiatura fields
- THEN the system returns 201

#### Scenario: S-TRA-005-4 — Update tipo from ADMINISTRATIVO to MEDICO requires CMP

- GIVEN a Trabajador with `tipo: "ADMINISTRATIVO"` and no colegiatura
- WHEN PUT `/api/v1/trabajadores/{id}` with `tipo: "MEDICO"` and no `nroColegiatura`
- THEN the system returns 422 with validation error

### Requirement: TRA-006 — Situación especial

The system MUST store `discapacidad` (boolean, default false) and `sindicalizado` (boolean, default false). Both SHALL be included in the response.

### Requirement: TRA-007 — CMP backward compatibility

The system MUST provide `Medico.getCmp()` that delegates to `Trabajador.nroColegiatura` when the Trabajador's `tipo` is MEDICO. The existing `med_cmp` column on `tb_medicos` SHALL be deprecated but preserved. Existing Medico endpoints MUST remain unchanged.

#### Scenario: S-TRA-007-1 — GET Medico returns CMP via Trabajador

- GIVEN a Medico linked to a Trabajador with `tipo: "MEDICO"` and `nroColegiatura: "CMP-12345"`
- WHEN GET `/api/v1/medicos/{id}`
- THEN the response includes `cmp: "CMP-12345"`
- AND the value originates from `Trabajador.nroColegiatura`

### Requirement: TRA-008 — Expanded response

The system MUST include all new fields in the `TrabajadorResponse` DTO: `tipo`, `regimenLaboral`, `fechaIngreso`, `fechaCese`, `motivoCese`, `banco`, `cuentaSueldo`, `CCI`, `contactoEmergenciaNombre`, `contactoEmergenciaTelefono`, `cantidadHijos`, `nroColegiatura`, `tipoColegiatura`, `discapacidad`, `sindicalizado`.

#### Scenario: S-TRA-008-1 — GET all trabajadores returns expanded fields

- GIVEN a Trabajador with all new fields populated
- WHEN GET `/api/v1/trabajadores`
- THEN each item in the response includes all expanded fields

### Requirement: TRA-009 — PII protection

The following fields MUST be excluded from `toString()`, logs, and serialization: `cuentaSueldo`, `CCI`, `contactoEmergenciaTelefono`. Use `@ToString.Exclude` and `@JsonIgnore` as appropriate.

### Requirement: TRA-010 — Sub-resource endpoints

The system MUST expose sub-resource endpoints under `/api/v1/trabajadores/{id}/contratos` and `/api/v1/trabajadores/{id}/derechohabientes` for their respective CRUD operations.
