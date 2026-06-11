# Clinica SOP Specification

## Purpose

Surgical procedure documentation: surgical reports (reporte operatorio), URPA (post-anesthesia recovery) monitoring, and integration with pre-defined surgical packages.

## Requirements

### Requirement: SOP-001 — Surgical report creation

The system MUST allow creating a surgical report (`ReporteOperatorio`) linked to a Hospitalizacion. The report SHALL include `fechaCirugia`, `horaInicio`, `horaFin`, `cirujanoId`, `anestesiologoId`, `diagnosticoPre`, `diagnosticoPost`, `procedimientoRealizado`, `hallazgos`, `complicaciones`, and `medicoId` (who documents it).

#### Scenario: SOP-001-1 — Create surgical report

- GIVEN a Hospitalizacion with status `HOSPITALIZADO` and an authenticated Medico
- WHEN POST `/api/v1/clinica/sop/reportes-operatorios` with valid surgical data and `hospitalizacionId`
- THEN the report is created with 201, linked to the Hospitalizacion
- AND the Hospitalizacion `tieneReporteOperatorio` flag is set to true

#### Scenario: SOP-001-2 — Missing required field

- GIVEN a request without `procedimientoRealizado`
- WHEN POST to create report
- THEN the system returns 422 with field validation error

#### Scenario: SOP-001-3 — Duplicate report for same hospitalization

- GIVEN a Hospitalizacion with an existing ReporteOperatorio
- WHEN POST creating another report for the same Hospitalizacion
- THEN the system returns 409 Conflict

### Requirement: SOP-002 — URPA recovery record

The system MUST allow creating URPA records per surgical report. URPA SHALL include `fechaIngresoURPA`, `fechaSalidaURPA`, `condicionIngreso`, `condicionSalida`, `escalaAldreteIngreso`, `escalaAldreteSalida`, and `observaciones`. Multiple URPA entries SHALL be allowed per report (multiple recovery phases).

#### Scenario: SOP-002-1 — Create URPA entry

- GIVEN an existing ReporteOperatorio
- WHEN POST `/api/v1/clinica/sop/reportes-operatorios/{id}/urpa` with `escalaAldreteIngreso: 7` and `condicionIngreso`
- THEN the URPA record is created with 201 and linked to the report

#### Scenario: SOP-002-2 — Invalid Aldrete scale

- GIVEN an `escalaAldreteIngreso` value of 12 (exceeds max 10)
- WHEN POST URPA entry
- THEN the system returns 422 with validation error

### Requirement: SOP-003 — Report immutability

Completed surgical reports (status `COMPLETADO`) MUST be immutable. The system SHALL allow editing only while status is `BORRADOR`. Status transitions: `BORRADOR` → `COMPLETADO` (one-way).

#### Scenario: SOP-003-1 — Edit in draft mode

- GIVEN a ReporteOperatorio with `estado: BORRADOR`
- WHEN PUT `/api/v1/clinica/sop/reportes-operatorios/{id}` with updated `hallazgos`
- THEN the update succeeds with 200

#### Scenario: SOP-003-2 — Edit completed report rejected

- GIVEN a ReporteOperatorio with `estado: COMPLETADO`
- WHEN PUT to update it
- THEN the system returns 423 Locked with "report already completed"

### Requirement: SOP-004 — Permission granularity

Endpoints MUST use `sop:{accion}` authorities: `sop:crear`, `sop:editar`, `sop:ver`, `sop:completar`. Only MEDICO role SHALL have `sop:crear` and `sop:completar`.

#### Scenario: SOP-004-1 — Non-medico cannot create report

- GIVEN a Usuario with role ENFERMERIA
- WHEN POST to create ReporteOperatorio
- THEN the system returns 403 Forbidden

### Requirement: SOP-005 — Data privacy

Surgical reports contain clinical PII. The system MUST exclude `hallazgos`, `complicaciones`, and `procedimientoRealizado` from generic list endpoints. Full report content SHALL only be returned via individual GET by authorized roles (MEDICO, ADMIN).

#### Scenario: SOP-005-1 — List hides clinical detail

- GIVEN a list of surgical reports
- WHEN GET `/api/v1/clinica/sop/reportes-operatorios` (list)
- THEN the response includes `id`, `fechaCirugia`, `cirujanoNombre` but NOT `hallazgos` or `complicaciones`
