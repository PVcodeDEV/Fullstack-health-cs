# Clinica Hospitalizacion Specification

## Purpose

In-hospital patient management: bed confirmation from solicitud, room changes during stay, nursing care records, evolution notes, medication requests, and clinical discharge.

## Requirements

### Requirement: HOSP-001 — Confirm bed from solicitud

The system MUST display pending `SolicitudHospitalizacion` records with status `CONFIRMADA` (bed already assigned by Admisión). Hospitalización SHALL confirm the bed to start the hospitalization period, changing solicitud status to `HOSPITALIZADO` and setting `fechaIngreso`.

#### Scenario: HOSP-001-1 — Confirm hospitalization

- GIVEN a `SolicitudHospitalizacion` with status `CONFIRMADA` and an assigned bed
- WHEN PUT `/api/v1/clinica/hospitalizacion/solicitudes/{id}/confirmar-ingreso`
- THEN status becomes `HOSPITALIZADO`, `fechaIngreso` is set, bed remains `OCUPADO`
- AND a `Hospitalizacion` record is created

#### Scenario: HOSP-001-2 — Confirm non-confirmed solicitud

- GIVEN a `SolicitudHospitalizacion` with status `PENDIENTE`
- WHEN confirming ingreso
- THEN the system returns 422 with solicitud-status validation error

### Requirement: HOSP-002 — Room change during stay

The system MUST allow changing a patient's bed during hospitalization. The new bed MUST match the patient's current `tipoHabitacionId`. On change, old bed becomes `DISPONIBLE`, new bed becomes `OCUPADO`. Each change SHALL be logged with `fechaCambio`, `camaOrigenId`, `camaDestinoId`, and `usuarioId`.

#### Scenario: HOSP-002-1 — Successful room change

- GIVEN a Hospitalizacion with `camaId: C-1` (type `HAB-1`) and bed `C-2` (type `HAB-1`, `DISPONIBLE`)
- WHEN PUT `/api/v1/clinica/hospitalizacion/{id}/cambiar-cama` with `camaId: C-2`
- THEN `C-1` becomes `DISPONIBLE`, `C-2` becomes `OCUPADO`, change is logged
- AND the system returns 200

#### Scenario: HOSP-002-2 — Room type mismatch on change

- GIVEN `C-2` has type `HAB-2` (different from current `HAB-1`)
- WHEN changing to `C-2`
- THEN the system returns 422

### Requirement: HOSP-003 — Nursing care notes

The system MUST allow registering nursing care notes (`NotaEnfermeria`) per Hospitalizacion. Each note SHALL include `fechaHora`, `usuarioId`, `descripcion`, `signosVitales` (JSON). Notes MUST be immutable after creation — no edit, no delete.

#### Scenario: HOSP-003-1 — Register nursing note

- GIVEN an active Hospitalizacion
- WHEN POST `/api/v1/clinica/hospitalizacion/{id}/notas-enfermeria` with `descripcion` and `signosVitales`
- THEN the note is created with 201 and `fechaHora` set server-side

#### Scenario: HOSP-003-2 — Immutable note

- GIVEN an existing NotaEnfermeria
- WHEN PUT to update it
- THEN the system returns 405 Method Not Allowed

### Requirement: HOSP-004 — Evolution notes

The system MUST allow registering evolution notes (`NotaEvolucion`) per Hospitalizacion. Each note SHALL include `fechaHora`, `usuarioId` (medico), `descripcion`, `plan`. Notes MUST be immutable.

#### Scenario: HOSP-004-1 — Register evolution note

- GIVEN an active Hospitalizacion and authenticated Medico
- WHEN POST `/api/v1/clinica/hospitalizacion/{id}/notas-evolucion` with valid data
- THEN the note is created with 201

### Requirement: HOSP-005 — Medication requests

The system MUST allow creating medication requests (`SolicitudMedicamento`) per Hospitalizacion. Each request SHALL include `medicamentoId`, `dosis`, `frecuencia`, `viaAdministracion`, `fechaInicio`, `fechaFin`, and `estado` (PENDIENTE/ATENDIDA/CANCELADA).

#### Scenario: HOSP-005-1 — Create medication request

- GIVEN an active Hospitalizacion
- WHEN POST `/api/v1/clinica/hospitalizacion/{id}/solicitudes-medicamento` with valid data
- THEN a request with `estado: PENDIENTE` is created

### Requirement: HOSP-006 — Clinical discharge

The system MUST support registering clinical discharge (`Alta`). Discharge SHALL set `fechaAlta`, `tipoAlta` (MEJORADO/VOLUNTARIO/TRASLADO/FALLECIDO), `diagnosticoFinal`, and `medicoId`. Discharge changes Hospitalizacion status to `ALTA_CLINICA`. Bed SHALL remain `OCUPADO` until Caja confirms payment.

#### Scenario: HOSP-006-1 — Clinical discharge registered

- GIVEN an active Hospitalizacion and authenticated Medico
- WHEN POST `/api/v1/clinica/hospitalizacion/{id}/alta` with `tipoAlta: MEJORADO` and `diagnosticoFinal`
- THEN Hospitalizacion status becomes `ALTA_CLINICA`, bed stays `OCUPADO`, Alta record is created

#### Scenario: HOSP-006-2 — Discharge without medico role

- GIVEN a Usuario with role ENFERMERIA
- WHEN POST alta
- THEN the system returns 403 Forbidden

### Requirement: HOSP-007 — Permission granularity

Endpoints MUST use `hospitalizacion:{accion}` where `accion` is `crear`, `editar`, `ver`, `eliminar`, `cambiar_cama`, `alta`.

### Requirement: HOSP-008 — PII data protection

All nursing and evolution note content MUST be treated as clinical PII. Notes MUST NOT appear in standard API list responses — only via explicit note retrieval endpoints.
