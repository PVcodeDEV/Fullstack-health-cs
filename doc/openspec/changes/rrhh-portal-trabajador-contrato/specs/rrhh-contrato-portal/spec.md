# RRHH Contrato Portal Specification

## Purpose

Thymeleaf/HTMX portal UI for Contrato lifecycle management (CRUD, resolver, suspender, reactivar) under `/administrativo/rrhh/contratos`. Contract state transitions are enforced server-side; the portal renders estado badges and available actions per state.

## Requirements

### Requirement: CPO-001 — Listado por trabajador

The portal MUST render a list of contratos for a given trabajador at `GET /administrativo/rrhh/contratos?trabajadorId={id}`. The list SHALL be accessible from the trabajador detail view's Contratos sub-tab and from a dedicated page at `/administrativo/rrhh/contratos` with a trabajador search/filter. Columns: Tipo, Fecha Inicio, Fecha Fin, Remuneración, Estado. Estado SHALL display as a colored badge.

#### Scenario: S-CPO-001-1 — List from trabajador detail

- GIVEN a trabajador with multiple contratos
- WHEN the user clicks "Contratos" in the trabajador detail view
- THEN the contratos list loads via HTMX with columns: Tipo, Inicio, Fin, Remuneración, Estado badge

#### Scenario: S-CPO-001-2 — Empty list

- GIVEN a trabajador has no contratos
- WHEN the contratos list loads
- THEN an empty state message is shown: "No tiene contratos registrados"

### Requirement: CPO-002 — Estado badges

The portal MUST render each contrato's `estado` as a colored badge: ACTIVO (green), CREADO (blue), SUSPENDIDO (yellow), VENCIDO (gray), RESUELTO (red). The badge SHALL use the same Tailwind safelist pattern as portal theme colors.

#### Scenario: S-CPO-002-1 — All estados render correct colors

- GIVEN contratos in each estado exist
- WHEN the list renders
- THEN each estado badge has the correct color class

### Requirement: CPO-003 — Crear contrato (modal)

The portal MUST provide a create form via HTMX modal at `GET /administrativo/rrhh/contratos/crear?trabajadorId={id}`. Fields: tipo, fechaInicio, fechaFin (conditional on tipo DETERMINADO), periodoPrueba, remuneracion, jornada. Validation: fechaFin required for DETERMINADO.

#### Scenario: S-CPO-003-1 — Create DETERMINADO succeeds

- GIVEN the user selects DETERMINADO and fills fechaInicio + fechaFin + remuneracion
- WHEN the form is submitted via HTMX POST
- THEN the modal closes, the list refreshes, and a success toast appears

#### Scenario: S-CPO-003-2 — DETERMINADO sin fechaFin fails

- GIVEN the user selects DETERMINADO without fechaFin
- WHEN the form is submitted
- THEN the modal stays open with error: "fechaFin es requerido para contratos DETERMINADO"

### Requirement: CPO-004 — Resolver, suspender, reactivar

The portal MUST provide action buttons per contrato row based on current estado: ACTIVO → "Resolver" and "Suspender"; SUSPENDIDO → "Reactivar" and "Resolver"; RESUELTO/VENCIDO → no actions. Each action SHALL open a confirmation modal via HTMX.

#### Scenario: S-CPO-004-1 — Resolver with motivo

- GIVEN a contrato with estado ACTIVO
- WHEN the user clicks "Resolver" and submits with a motivo
- THEN the list refreshes showing estado RESUELTO (red badge)
- AND the action buttons are hidden

#### Scenario: S-CPO-004-2 — Suspender y reactivar

- GIVEN a contrato with estado ACTIVO
- WHEN the user clicks "Suspender" and confirms
- THEN estado changes to SUSPENDIDO (yellow badge)
- AND "Reactivar" and "Resolver" buttons appear
- WHEN the user clicks "Reactivar" and confirms
- THEN estado returns to ACTIVO (green badge)

### Requirement: CPO-005 — Detalle de contrato

The portal MUST render a detail view at `GET /administrativo/rrhh/contratos/{id}` showing all contrato fields: tipo, fechas, periodoPrueba, remuneracion, jornada, estado, and the linked trabajador info.

#### Scenario: S-CPO-005-1 — Detail shows all fields

- GIVEN a contrato exists with all fields populated
- WHEN the user navigates to the detail view
- THEN all fields are displayed including the estado badge and trabajador name

### Requirement: CPO-006 — Permisos

All contrato portal endpoints SHALL require `hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')`. Create, edit, resolver, suspender, and reactivar SHALL additionally require `hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')`.

#### Scenario: S-CPO-006-1 — Write action denied

- GIVEN a user with only `administrativo:ver`
- WHEN they attempt to resolver a contrato
- THEN the action returns 403 with an error message
