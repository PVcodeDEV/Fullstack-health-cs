# Persona Specification

## Purpose

Central entity shared by Paciente, Trabajador, Medico, and Cliente roles. Provides CRUD, document validation, API-based auto-fill rules, and PII protection per Ley 29733.

## Requirements

### Requirement: R-001 — Unique document identity
The system MUST reject creation if `tipoDocumentoIdentidad` + `numeroDocumento` are not provided. The pair MUST be unique. Both MUST be NOT NULL.

#### Scenario: SC-001 — Create with DNI via API
- GIVEN a DNI with valid check digit and API reachable
- WHEN POST `/api/v1/personas` with `tipoDocumentoIdentidad: 1` (DNI) and `numeroDocumento: "12345678"`
- THEN the system calls SUNAT API, fills `nombres` + `apellidos`, user completes remaining fields
- AND the system returns 201 with the Persona record

#### Scenario: SC-002 — Create with CE (manual)
- GIVEN a Carnet de Extranjería
- WHEN POST `/api/v1/personas` with `tipoDocumentoIdentidad: 2` (CE) and `numeroDocumento: "CE001234567890"`
- THEN all name and address fields MUST be entered manually
- AND the system returns 201

#### Scenario: SC-003 — Duplicate numeroDocumento
- GIVEN a Persona with `numeroDocumento: "12345678"` already exists
- WHEN POST `/api/v1/personas` with the same `numeroDocumento`
- THEN the system returns 409 Conflict

### Requirement: R-002 — DNI check digit (módulo 11)
The system MUST validate DNI check digit via `Modulo11Validator` before persisting. Invalid check digit MUST reject with 422.

#### Scenario: SC-002-1 — Invalid check digit rejected
- GIVEN the DNI type with módulo 11 validation
- WHEN POST with `numeroDocumento: "12345678"` (invalid check digit)
- THEN the system returns 422 Unprocessable Entity with validation error

### Requirement: R-003 — CE format validation
The system MUST validate CE numbers are exactly 12 characters. Non-conforming values MUST be rejected.

#### Scenario: SC-003-1 — Short CE rejected
- GIVEN a Carnet de Extranjería type
- WHEN POST with `numeroDocumento: "CE001234"` (8 chars, not 12)
- THEN the system returns 422 with format validation error

### Requirement: R-004 — Multiple roles
A single Persona MUST be referenceable by multiple role tables (tb_pacientes, tb_trabajadores, tb_medicos, tb_clientes) simultaneously via FK `persona_id`.

### Requirement: R-005 — Full editability
All Persona fields (`nombres`, `apellidos`, `fechaNacimiento`, `sexo`, `direccion`, `telefono`, `email`, etc.) MUST be updatable after creation via PUT/PATCH.

### Requirement: R-006 — Manual PII fields
The system MUST NOT auto-fill `pers_telefono` or `pers_email` from any API. These fields SHALL only be set manually by the user.

### Requirement: R-007 — Refresh on stale data
When querying a DNI where `pers_fecha_ultima_consulta` exceeds 1 year, the system SHOULD call the configured API to refresh names and addresses. The `pers_fecha_ultima_consulta` timestamp MUST be updated on each successful API call.

#### Scenario: SC-007-1 — Stale DNI triggers refresh
- GIVEN a Persona with DNI and `fechaUltimaConsulta` older than 1 year
- WHEN GET `/api/v1/personas/{id}`
- THEN the system asynchronously calls the API to refresh names and address
- AND updates `fechaUltimaConsulta` on success

### Requirement: R-008 — PII data protection
The following fields MUST NOT appear in logs, `toString()`, serialization, or API error responses: `numeroDocumento`, `nombres`, `apellidos`, `direccion`, `telefono`, `email`. Use `@ToString.Exclude`, `@JsonIgnore`, or equivalent. Error responses MUST use anonymized references.

#### Scenario: SC-008-1 — Logged request excludes PII
- GIVEN a Persona creation request with PII data
- WHEN the service logs a debug message
- THEN the log output MUST NOT contain `numeroDocumento`, `nombres`, `apellidos`, `direccion`, `telefono`, or `email`

### Requirement: R-009 — Persona search
The system MUST support searching Personas by `numeroDocumento` (exact match) and by `nombres`/`apellidos` (ILIKE partial match). Inactive Personas (`pers_activo = false`) MUST be excluded from default searches.

#### Scenario: SC-004 — Search by DNI
- GIVEN a Persona with `numeroDocumento: "12345678"`
- WHEN GET `/api/v1/personas?numeroDocumento=12345678`
- THEN the system returns the matching Persona with full data

#### Scenario: SC-005 — Inactive excluded from search
- GIVEN a Persona with `activo: false`
- WHEN GET `/api/v1/personas?nombres=Juan`
- THEN the system does NOT return the inactive Persona in results
