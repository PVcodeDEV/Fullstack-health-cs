# Clinica Admision Specification

## Purpose

Patient admission: account creation, surgical package selection, auto-generated hospitalization request, bed assignment, HC creation, and CIE-11 diagnosis registration.

## Requirements

### Requirement: ADM-001 — Patient search

The system MUST search patients by `numeroDocumento` (exact), `numeroHistoriaClinica` (exact), or `nombres`/`apellidos` (ILIKE). Admisión selects from results to create an account.

#### Scenario: ADM-001-1 — Search by DNI

- GIVEN a Paciente with `numeroDocumento: "12345678"`
- WHEN GET `/api/v1/clinica/admision/pacientes?numeroDocumento=12345678`
- THEN the system returns the matching record

#### Scenario: ADM-001-2 — No results

- GIVEN no patient matches
- WHEN GET with non-existing DNI
- THEN the system returns 200 with empty array

### Requirement: ADM-002 — Account with surgical package

The system MUST create a `Cuenta` with the selected surgical package (includes `tipoHabitacionId`). Package price MUST NOT be visible to Admisión. `tipoHabitacion` determines assignable beds.

#### Scenario: ADM-002-1 — Create account

- GIVEN a selected Paciente and active package with `tipoHabitacionId: HAB-1`
- WHEN POST `/api/v1/clinica/admision/cuentas` with `pacienteId` and `paqueteQuirurgicoId`
- THEN a Cuenta is created with the package's `tipoHabitacionId`
- AND the response excludes package price

#### Scenario: ADM-002-2 — Invalid package

- GIVEN an inactive or missing package
- WHEN POST with that `paqueteQuirurgicoId`
- THEN the system returns 422

### Requirement: ADM-003 — Auto-generated solicitud

Account creation MUST auto-generate a `SolicitudHospitalizacion` with `cuentaId`, `tipoHabitacionId`, status `PENDIENTE`. Bed assignment is a separate step.

#### Scenario: ADM-003-1 — Auto-generation

- GIVEN a Cuenta created with a surgical package
- WHEN the account is persisted
- THEN a SolicitudHospitalizacion with status `PENDIENTE` is created
- AND `solicitudId` is returned in the response

### Requirement: ADM-004 — Bed assignment

Admisión MUST assign a specific bed to the solicitud. Only beds matching `tipoHabitacionId` with status `DISPONIBLE` SHALL be selectable. On assignment: bed → `OCUPADO`, solicitud → `CONFIRMADA`.

#### Scenario: ADM-004-1 — Assign available bed

- GIVEN a solicitud with `tipoHabitacionId: HAB-1` and a bed with `HAB-1, estado: DISPONIBLE`
- WHEN PUT `/api/v1/clinica/admision/solicitudes/{id}/asignar-cama` with `camaId`
- THEN bed becomes `OCUPADO`, solicitud becomes `CONFIRMADA`

#### Scenario: ADM-004-2 — Bed room type mismatch

- GIVEN a bed with `tipoHabitacionId: HAB-2` (differs from solicitud)
- WHEN assigning it
- THEN the system returns 422

#### Scenario: ADM-004-3 — Bed occupied

- GIVEN a bed with `estado: OCUPADO`
- WHEN assigning it
- THEN the system returns 409

### Requirement: ADM-005 — HC creation

The system MUST create a `HistoriaClinica` on first admission if none exists. HC number MUST be auto-generated with `HC-` prefix.

#### Scenario: ADM-005-1 — New HC

- GIVEN a Paciente with no existing HC
- WHEN account creation completes with bed assigned
- THEN a HistoriaClinica is created with `numeroHistoriaClinica: "HC-00001"`

### Requirement: ADM-006 — CIE-11 diagnosis

The system MUST allow registering diagnosis codes on the account. Each diagnosis SHALL include `codigoCIE11`, `descripcion`, `tipo` (PRINCIPAL/SECUNDARIO).

#### Scenario: ADM-006-1 — Register diagnosis

- GIVEN an existing Cuenta
- WHEN POST `/api/v1/clinica/admision/cuentas/{id}/diagnosticos` with `{ codigoCIE11: "4A00", tipo: "PRINCIPAL" }`
- THEN the diagnosis is created (201)

#### Scenario: ADM-006-2 — Invalid CIE-11 code

- GIVEN a non-existing CIE-11 code
- WHEN registering it
- THEN the system returns 422

### Requirement: ADM-007 — Permissions

Endpoints MUST use `@PreAuthorize` with `admision:{accion}` where `accion` is `crear`, `editar`, `ver`, `eliminar`, `asignar_cama`.

#### Scenario: ADM-007-1 — 403 on missing permiso

- GIVEN a Usuario without `admision:asignar_cama`
- WHEN PUT to assign a bed
- THEN the system returns 403

### Requirement: ADM-008 — PII protection

Search results MUST NOT expose full `numeroDocumento` in logs. `nombres`, `apellidos`, `direccion`, `telefono`, `email` MUST use `@ToString.Exclude`.
