# RRHH Trabajador Portal Specification

## Purpose

Thymeleaf/HTMX portal UI for Trabajador CRUD, reingreso, and sub-resource views (contratos, periodos laborales) under `/administrativo/rrhh/trabajadores`. Backend REST API at `/api/v1/trabajadores` is consumed via direct service calls.

## Requirements

### Requirement: TPO-001 — Listado con filtros

The portal MUST render a paginated list of active trabajadores at `GET /administrativo/rrhh/trabajadores`. The list SHALL include columns: Documento, Nombres, Tipo, Régimen, Cargo, Fecha Ingreso, Estado. The list SHALL support filtering by tipo, regimenLaboral, and search by nombre/apellido via HTMX partial refresh.

#### Scenario: S-TPO-001-1 — List renders with data

- GIVEN trabajadores exist in the database
- WHEN the user navigates to `/administrativo/rrhh/trabajadores`
- THEN a table with trabajador rows is displayed
- AND each row shows Documento, Nombres, Tipo, Régimen, Cargo, Fecha Ingreso

#### Scenario: S-TPO-001-2 — Filter updates list via HTMX

- GIVEN the list page is loaded
- WHEN the user selects a `tipo` filter
- THEN the table body is replaced via HTMX with filtered results
- AND the URL is updated

#### Scenario: S-TPO-001-3 — Empty state

- GIVEN no trabajadores match the current filter
- WHEN the list loads
- THEN an empty state message is shown: "No se encontraron trabajadores"

### Requirement: TPO-002 — Crear trabajador (modal)

The portal MUST provide a create form via HTMX modal at `GET /administrativo/rrhh/trabajadores/crear`. The form SHALL include fields from `TrabajadorRequest`: personaId (search/select), tipo, regimenLaboral, fechaIngreso, cargo, and optional fields (banco, cuentaSueldo, CCI, contactoEmergencia, cantidadHijos, nroColegiatura, tipoColegiatura, discapacidad). The form SHALL validate: colegiatura required for MEDICO/ENFERMERA/NUTRICIONISTA.

#### Scenario: S-TPO-002-1 — Create succeeds

- GIVEN the user opens the create modal and fills valid data
- WHEN the user submits the form via HTMX POST
- THEN the modal closes, the list refreshes, and a success toast appears
- AND a new trabajador row appears in the list

#### Scenario: S-TPO-002-2 — Validation errors shown inline

- GIVEN the user submits with missing required fields (e.g., MEDICO without CMP)
- WHEN the form is submitted
- THEN the modal stays open with field-level error messages
- AND no trabajador is created

### Requirement: TPO-003 — Editar trabajador

The portal MUST provide an edit view at `GET /administrativo/rrhh/trabajadores/{id}/editar` using the same modal form pattern as create, pre-populated with existing values.

#### Scenario: S-TPO-003-1 — Edit and save

- GIVEN a trabajador exists
- WHEN the user opens edit, changes `cargo`, and submits
- THEN the list refreshes showing the updated cargo

### Requirement: TPO-004 — Detalle de trabajador

The portal MUST render a detail view at `GET /administrativo/rrhh/trabajadores/{id}` showing all trabajador fields plus sub-tabs for Contratos and Periodos Laborales.

#### Scenario: S-TPO-004-1 — Detail with sub-tabs

- GIVEN a trabajador with contratos and periodos
- WHEN the user navigates to the detail view
- THEN the main info is displayed
- AND clicking "Contratos" loads the contratos sub-tab via HTMX
- AND clicking "Periodos" loads the periodos sub-tab via HTMX

### Requirement: TPO-005 — Reingreso

The portal MUST provide a reingreso action at `POST /administrativo/rrhh/trabajadores/{id}/reingreso` via a confirmation modal with fechaInicio field.

#### Scenario: S-TPO-005-1 — Reingreso success

- GIVEN a trabajador with a previous cese date
- WHEN the user confirms reingreso with a future fechaInicio
- THEN the detail view updates showing the new periodo activo
- AND a success toast confirms the action

### Requirement: TPO-006 — Permisos

All portal endpoints SHALL require `hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')`. Create, edit, delete, and reingreso SHALL additionally require `hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')`.

#### Scenario: S-TPO-006-1 — Read access denied

- GIVEN a user without `administrativo:ver`
- WHEN they navigate to `/administrativo/rrhh/trabajadores`
- THEN they receive a 403 error page
