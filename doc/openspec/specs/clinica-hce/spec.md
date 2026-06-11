# Clinica HCE Specification

## Purpose

Digital clinical documents for Electronic Health Records (HCE). Each document carries an internal digital signature chain. Content in DB via BYTEA for MVP.

## Requirements

### Requirement: HCE-001 — Document types

The system MUST support: `FILIACION`, `CONSENTIMIENTO`, `HC`, `EVOLUCION`, `REPORTE_OPERATORIO`, `EPICRISIS`, `KARDEX`, `NOTA_ENFERMERIA`. Each SHALL be stored in `tb_documentos_hce` with `tipoDocumento`, `pacienteId`, `hospitalizacionId` (nullable), `contenido` (BYTEA).

#### Scenario: HCE-001-1 — Create document

- GIVEN an authenticated MEDICO
- WHEN POST `/api/v1/clinica/hce/documentos` with `tipoDocumento`, `pacienteId`, and `contenido`
- THEN the document is persisted with 201

#### Scenario: HCE-001-2 — Invalid type

- GIVEN a `tipoDocumento` not in the enum
- WHEN POST
- THEN the system returns 422

### Requirement: HCE-002 — Internal digital signature

Each document creation SHALL generate a signature record in `tb_firmas_digitales`: `documentoId`, `usuarioId`, `fechaFirma` (server timestamp), `hashDocumento` (SHA-256 of `contenido`), `ipOrigen`. Hash MUST be computed server-side. Signature SHALL be immutable.

#### Scenario: HCE-002-1 — Signature on creation

- GIVEN a document with byte content
- WHEN the document is persisted
- THEN a FirmaDigital is created with correct hash, `usuarioId`, timestamp, and request IP

#### Scenario: HCE-002-2 — Signature verification

- GIVEN a document and its FirmaDigital
- WHEN GET `/api/v1/clinica/hce/documentos/{id}/verificar-firma`
- THEN the system recomputes SHA-256, compares with stored hash, and returns `{ valida: true/false, fechaFirma, usuarioId }`

### Requirement: HCE-003 — Document immutability

Documents SHALL be immutable after creation. No PUT, PATCH, or DELETE. A new version SHALL be a new row with `documentoOriginalId` referencing the prior version.

#### Scenario: HCE-003-1 — New version

- GIVEN document D-1
- WHEN POST with `documentoOriginalId: D-1` and new `contenido`
- THEN D-2 is created with `documentoOriginalId: D-1`
- AND both are retrievable

#### Scenario: HCE-003-2 — Delete rejected

- GIVEN an existing document
- WHEN DELETE
- THEN the system returns 405

### Requirement: HCE-004 — Retrieval by patient

The system MUST list documents by `pacienteId` with optional `tipoDocumento` filter. List responses SHALL exclude `contenido`. Full content on individual GET only.

#### Scenario: HCE-004-1 — List by patient

- GIVEN a Paciente with 3 documents
- WHEN GET `/api/v1/clinica/hce/documentos?pacienteId=P-1`
- THEN 3 metadata entries are returned without `contenido`

#### Scenario: HCE-004-2 — Filter by type

- WHEN GET with `pacienteId=P-1&tipoDocumento=EVOLUCION`
- THEN only EVOLUCION documents are returned

### Requirement: HCE-005 — Permissions

Endpoints MUST use `hce:{accion}`: `crear`, `ver`, `ver_contenido`, `verificar`. `hce:crear` SHALL be MEDICO/ENFERMERIA. `hce:ver_contenido` SHALL be MEDICO/ADMIN.

#### Scenario: HCE-005-1 — RECEPCION denied content

- GIVEN a Usuario with role RECEPCION
- WHEN GET `/api/v1/clinica/hce/documentos/{id}`
- THEN 403 Forbidden (content access)
- WHEN GET list (metadata only)
- THEN 200 with metadata

### Requirement: HCE-006 — Data privacy (Ley 29733)

The system MUST NOT log `contenido`. Error responses MUST NOT include document content. Audit log SHALL record every access: `usuarioId`, `documentoId`, `fechaAcceso`, `accion` (CREAR/VER/VERIFICAR).

#### Scenario: HCE-006-1 — Content excluded from logs

- GIVEN a document retrieval
- WHEN the application logs it
- THEN `contenido` MUST NOT appear in any log line

#### Scenario: HCE-006-2 — Audit trail

- GIVEN a MEDICO views document D-1
- WHEN GET the full document
- THEN an audit entry is created with `accion: VER`

### Requirement: HCE-007 — Signature password challenge

Before creating a `FirmaDigital`, the signing user MUST provide a password. The system SHALL validate the password against the expected formula and reject the signature on mismatch. This is an explicit confirmation — the doctor actively asserts their identity and patient context.

The system MUST compare the user-provided password against the expected value using a constant-time comparison to prevent timing attacks. The password MUST NOT be stored; only verified ephemerally at signing time.

> **DECISIÓN PENDIENTE**: La fórmula exacta de la contraseña está por definir. Opciones:
>
> | Opción | Fórmula | Implicancia |
> |--------|---------|-------------|
> | **A** | `concat(pacienteDNI, medicoDNI)` | Ata la firma al par doctor-paciente. El médico necesita saber ambos DNIs. |
> | **B** | `medicoDNI` (solo el del doctor) | Más simple. Cualquier médico puede firmar sin conocer el DNI del paciente. |
> | **C** | Otra combinación | Evaluar según requerimiento. |

- `paciente.numeroDocumento` is obtained from the clinical document's patient record
- `medico.numeroDocumento` is obtained from the authenticated user's associated Persona/Medico record

#### Scenario: HCE-007-1 — Correct password signs (asumiendo Opción A)

- GIVEN a pending document (no signature) for Paciente with DNI `12345678`
- AND the signing Medico has DNI `44444444`
- WHEN POST `/api/v1/clinica/hce/documentos/{id}/firmar` with `password: "1234567844444444"`
- THEN a `FirmaDigital` is created with 201

#### Scenario: HCE-007-2 — Wrong password rejected

- GIVEN the same context
- WHEN POST with `password: "wrongpassword"`
- THEN the system returns 401/403 with error `FIRMA_PASSWORD_INCORRECTA`
- AND no `FirmaDigital` is created

#### Scenario: HCE-007-3 — Already signed document

- GIVEN a document that already has a `FirmaDigital`
- WHEN POST to sign it again
- THEN the system returns 409 `DOCUMENTO_YA_FIRMADO`
