# Portal Admisiones Specification

## Purpose

Portal Asistencial multi-step wizard for patient admission. Guides Admisión through: (1) patient search/selection, (2) surgical package selection, (3) optional bed assignment, (4) CIE-11 diagnosis registration. Includes 2-hour PENDIENTE alert (ADM-009).

## Requirements

### Requirement: PA-001 — Multi-step wizard with progress indicator

The system MUST render a 4-step wizard with visible progress indicator showing current step (1 of 4, 2 of 4, etc.). Steps: 1) Buscar Paciente, 2) Seleccionar Paquete, 3) Asignar Cama, 4) Registrar Diagnóstico.

#### Scenario: PA-001-1 — Wizard loads with step 1 active

- GIVEN user navigates to `/portal/admisiones/nueva`
- WHEN page loads
- THEN step 1 "Buscar Paciente" is active
- AND progress indicator shows "1 de 4"
- AND steps 2-4 are disabled until prior step completes

### Requirement: PA-002 — Step 1: Search and select patient

Step 1 MUST provide the same search functionality as Portal Pacientes (PP-001). Selecting a patient advances to step 2 and stores `pacienteId` in wizard state.

#### Scenario: PA-002-1 — Search DNI works, select patient moves to step 2

- GIVEN wizard at step 1
- WHEN user searches DNI "12345678" and selects the result
- THEN wizard advances to step 2
- AND `pacienteId` is stored in wizard state
- AND progress indicator shows "2 de 4"

### Requirement: PA-003 — Step 2: Select surgical package (name + "1 día incluido", NO price)

Step 2 MUST list active surgical packages. Each package MUST display `nombre` and the badge "1 día incluido" (derived from `diasIncluidos`). Package price MUST be excluded from the API response for ADMISION role (per ADM-002).

#### Scenario: PA-003-1 — Packages list loads filtered by active status

- GIVEN wizard at step 2 with `pacienteId` stored
- WHEN step 2 loads
- THEN system calls GET `/api/v1/clinica/admision/paquetes?activo=true`
- AND displays only active packages with `nombre` and "1 día incluido" badge
- AND price field is NOT present in response

#### Scenario: PA-003-2 — Price is excluded from response for ADMISION role

- GIVEN an ADMISION user at step 2
- WHEN packages list loads
- THEN response JSON for each package does NOT contain `precio` or `costo` field

### Requirement: PA-004 — Step 3: Assign bed (optional, can be skipped)

Step 3 MUST show available beds filtered by `tipoHabitacionId` from selected package. User MAY skip bed assignment. If skipped, a `SolicitudHospitalizacion` is created with status `PENDIENTE` and the 2-hour alert timer starts (ADM-009).

#### Scenario: PA-004-1 — Available beds filter by room type from package

- GIVEN wizard at step 3 with package having `tipoHabitacionId: HAB-1`
- WHEN step 3 loads
- THEN system calls GET `/api/v1/clinica/admision/camas?tipoHabitacionId=HAB-1&estado=DISPONIBLE`
- AND displays only beds matching that room type with status DISPONIBLE

#### Scenario: PA-004-2 — Skip bed assignment creates PENDIENTE solicitud

- GIVEN wizard at step 3
- WHEN user clicks "Saltar asignación de cama"
- THEN system POSTs `/api/v1/clinica/admision/cuentas` with `pacienteId` and `paqueteQuirurgicoId`
- AND a SolicitudHospitalizacion with status `PENDIENTE` is created (ADM-003)
- AND 2-hour alert timer starts (ADM-009)
- AND wizard advances to step 4

#### Scenario: PA-004-3 — System shows warning if solicitud pending >2 hours

- GIVEN a PENDIENTE solicitud created >2 hours ago
- WHEN user opens the wizard or views admissions list
- THEN the system displays warning banner: "Admisión pendiente de cama por más de 2 horas"
- AND warning links to the solicitud for bed assignment

### Requirement: PA-005 — Step 4: Register CIE-11 diagnosis (PRINCIPAL/SECUNDARIO)

Step 4 MUST provide autocomplete search for CIE-11 codes. User selects code, enters description, chooses type (PRINCIPAL or SECUNDARIO), and submits. Diagnosis is registered on the Cuenta.

#### Scenario: PA-005-1 — Autocomplete works for CIE-11 code search

- GIVEN wizard at step 4 with Cuenta created
- WHEN user types "4A0" in diagnosis code field
- THEN system calls GET `/api/v1/clinica/cie11?q=4A0`
- AND returns matching codes with descriptions for autocomplete dropdown

#### Scenario: PA-005-2 — Diagnosis registered successfully

- GIVEN wizard at step 4 with autocomplete suggestion selected
- WHEN user submits with `codigoCIE11: "4A00"`, `descripcion: "Neumonía bacteriana"`, `tipo: "PRINCIPAL"`
- THEN system POSTs `/api/v1/clinica/admision/cuentas/{id}/diagnosticos`
- AND diagnosis is created (201)
- AND wizard shows completion screen with admission summary

### Requirement: PA-006 — Access control for portal admisiones

The Portal Admisiones wizard MUST enforce permissions: `admision:ver` (view), `admision:crear` (create account/package), `admision:asignar_cama` (bed assignment). Missing permissions return 403.

#### Scenario: PA-006-1 — User with proper permission sees the wizard

- GIVEN an authenticated user with `admision:crear` and `admision:asignar_cama`
- WHEN user navigates to `/portal/admisiones/nueva`
- THEN the wizard loads with all 4 steps accessible

#### Scenario: PA-006-2 — 403 on missing permission

- GIVEN an authenticated user WITHOUT `admision:asignar_cama`
- WHEN user reaches step 3 and attempts to assign or skip bed
- THEN the system returns 403 Forbidden
- AND wizard shows error message: "No tiene permiso para asignar cama"