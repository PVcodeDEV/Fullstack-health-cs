# Delta for clinica-admision

## ADDED Requirements

### Requirement: ADM-009 — PENDIENTE solicitud 2-hour alert

The system MUST warn when a `SolicitudHospitalizacion` remains in status `PENDIENTE` for more than 2 hours. Warning SHALL be visible in the Portal Asistencial Admisiones wizard and via backend event.

#### Scenario: ADM-009-1 — Warning after 2 hours

- GIVEN a SolicitudHospitalizacion created with status `PENDIENTE` at T0
- WHEN current time exceeds T0 + 2 hours and solicitud is still `PENDIENTE`
- THEN the system SHALL emit a warning event `SOLICITUD_PENDIENTE_EXCEEDS_2H`
- AND the Portal Asistencial wizard SHALL display a warning banner for that solicitud

#### Scenario: ADM-009-2 — No warning when confirmed

- GIVEN a SolicitudHospitalizacion with status `CONFIRMADA`
- WHEN 2 hours pass since creation
- THEN the system MUST NOT emit the warning event

## MODIFIED Requirements

### Requirement: ADM-003 — Auto-generated solicitud

Account creation MUST auto-generate a `SolicitudHospitalizacion` with `cuentaId`, `tipoHabitacionId`, status `PENDIENTE`. Bed assignment is a separate step. If bed is not assigned within 2 hours, the system MUST warn via `SOLICITUD_PENDIENTE_EXCEEDS_2H` event.
(Previously: Auto-generation created PENDIENTE solicitud but no alert requirement)

#### Scenario: ADM-003-1 — Auto-generation

- GIVEN a Cuenta created with a surgical package
- WHEN the account is persisted
- THEN a SolicitudHospitalizacion with status `PENDIENTE` is created
- AND `solicitudId` is returned in the response
- AND a 2-hour timer starts for PENDIENTE alert

#### Scenario: ADM-003-2 — Timer resets on bed assignment

- GIVEN a PENDIENTE solicitud with timer running
- WHEN PUT `/api/v1/clinica/admision/solicitudes/{id}/asignar-cama` succeeds
- THEN solicitud becomes `CONFIRMADA` and timer is cancelled
- AND no warning event is emitted