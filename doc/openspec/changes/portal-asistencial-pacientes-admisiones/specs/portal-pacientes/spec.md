# Portal Pacientes Specification

## Purpose

Portal Asistencial patient search and detail view. Allows Admisión and Médico roles to search patients by DNI or name, view demographics, and see linked admissions history.

## Requirements

### Requirement: PP-001 — Search pacientes by DNI or name

The system MUST search patients by `numeroDocumento` (exact match) or `nombres`/`apellidos` (ILIKE partial match) from the portal page. Results MUST include `id`, `numeroDocumento`, `nombres`, `apellidos` from the Persona search endpoint (PER-009).

#### Scenario: PP-001-1 — Search by DNI returns matching patient

- GIVEN a Paciente with `numeroDocumento: "12345678"`
- WHEN user enters "12345678" in the search field and submits
- THEN the system calls GET `/api/v1/personas?numeroDocumento=12345678`
- AND displays the matching patient in results table

#### Scenario: PP-001-2 — Search by partial name returns results

- GIVEN pacientes with `nombres: "Juan"`, `apellidos: "Pérez"` and `nombres: "Juana"`, `apellidos: "Gómez"`
- WHEN user enters "Juan" in the search field
- THEN the system calls GET `/api/v1/personas?nombres=Juan`
- AND displays both matching patients in results table

#### Scenario: PP-001-3 — No results returns empty state message

- GIVEN no pacientes match the search criteria
- WHEN user submits search
- THEN the system displays "No se encontraron pacientes" message
- AND no results table is shown

### Requirement: PP-002 — View patient detail with demographics and admissions

The system MUST display a patient detail view showing demographics (`numeroDocumento`, `nombres`, `apellidos`, `fechaNacimiento`, `sexo`, `direccion`, `telefono`, `email`) and a list of linked admissions (`Cuenta` records) with status, package name, admission date, and bed assignment.

#### Scenario: PP-002-1 — Click patient row shows detail view

- GIVEN search results with a patient row
- WHEN user clicks the row or "Ver detalle" button
- THEN the system navigates to `/portal/pacientes/{id}`
- AND displays demographics and admissions list

#### Scenario: PP-002-2 — Patient with 0 admissions shows empty list

- GIVEN a patient with no linked Cuenta records
- WHEN detail view loads
- THEN admissions section shows "No tiene admisiones registradas"
- AND no table rows are rendered

### Requirement: PP-003 — Access control for portal pacientes

The Portal Pacientes page MUST be accessible only to users with role `ADMISION` or `MEDICO` (or equivalent permission `paciente:ver`). Unauthorized users MUST receive 403.

#### Scenario: PP-003-1 — User with `paciente:ver` sees the page

- GIVEN an authenticated user with `paciente:ver` permission
- WHEN user navigates to `/portal/pacientes`
- THEN the page loads with search and results

#### Scenario: PP-003-2 — Unauthorized role gets 403

- GIVEN an authenticated user WITHOUT `paciente:ver` permission
- WHEN user navigates to `/portal/pacientes`
- THEN the system returns 403 Forbidden
- AND user is redirected to access denied page