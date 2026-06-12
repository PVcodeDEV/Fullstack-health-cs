# Spec: Módulo Entidad

## Description

Legal entity (Empresa) management for RUC-based operations — both **clients** (Factura issuance) and **suppliers** (purchase registration, pharmacy procurement). Lives as an independent module (`com.clinica.entidad`) because both Caja and Farmacia consume it. Empresas are identified by RUC with two tiers: RUC 10 (persona natural, linked to existing `Persona` record) and RUC 20 (persona jurídica, standalone legal entity).

SUNAT RUC API integration provides automatic data fetch for both tiers. Role assignment (CLIENTE / PROVEEDOR / AMBOS) is automatic based on usage context — the user never selects it manually.

Base package: `com.clinica.entidad`.

## Cross-References

- **CPR-001** (caja-comprobante): Factura issuance requires an Empresa with RUC, razón social, and dirección fiscal.
- **LIQ-001** (caja-liquidacion): Payment to suppliers (proveedores) references Empresa as payee.
- **Farmacia**: Pharmacy procurement will reference Empresa as supplier (laboratorios, vendedores). Future module.
- **MAESTRO-R-001** (maestro-catalogos-financieros): TipoDocumentoIdentidad seeded with RUC (06) for SUNAT.
- **PERSONA**: Persona natural (DNI/CE/Pasaporte) may link to Empresa when the same person operates as RUC 10.

## Requirements

### Requirement: ENT-001 — Empresa entity definition

- **Priority**: MUST
- **Description**: The system MUST store legal entities with at least: `ruc` (11 digits, PK, validated with módulo 11 checksum), `tipoRuc` (RUC_10 / RUC_20), `razonSocial` (required for RUC 20, optional for RUC 10 where `apenomdenunciado` from SUNAT API is used), `direccionFiscal` (required for RUC 20), `ubigeo` (nullable, SUNAT ubigeo code from the fetched API data — only available for RUC 20), `telefono` (nullable), `email` (nullable), `estado` (ACTIVO/INACTIVO, defaults to ACTIVO), `rol` (CLIENTE/PROVEEDOR/AMBOS — assigned automatically by context), `personaId` (nullable FK to Persona, set when RUC 10 matches an existing person). RUC validation uses módulo 11 (same algorithm as DNI validation). The RUC format is 11 digits: 10 for persona natural (primer dígito = 1), 20 for persona jurídica (primer dígito = 2).
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/entidad/empresa` with valid RUC 20 returns 201 with full empresa data
  - [ ] RUC with invalid módulo 11 returns 400
  - [ ] RUC 10 with existing matching Persona auto-links `personaId`
  - [ ] RUC duplicates are rejected with 409
  - [ ] `razonSocial` and `direccionFiscal` required for RUC 20, optional for RUC 10

#### Scenario: ENT-001-1 — Create RUC 20 empresa

- **GIVEN** a valid RUC 20 "20123456789" with módulo 11 valid
- **WHEN** POST `/api/v1/entidad/empresa` with `{ ruc: "20123456789", razonSocial: "Clínica Ejemplo SAC", direccionFiscal: "Av. Principal 123, Lima", tipoRuc: "RUC_20" }`
- **THEN** the response is 201 with the empresa created
- **AND** `rol` defaults to CLIENTE (first context usage will promote it if needed)
- **AND** `estado` is ACTIVO

#### Scenario: ENT-001-2 — Invalid RUC módulo 11

- **GIVEN** RUC "20123456788" (invalid checksum)
- **WHEN** POST `/api/v1/entidad/empresa`
- **THEN** the response is 400 — invalid RUC checksum

#### Scenario: ENT-001-3 — Duplicate RUC rejected

- **GIVEN** Empresa exists with RUC "20123456789"
- **WHEN** POST the same RUC again
- **THEN** the response is 409 — RUC already exists

#### Scenario: ENT-001-4 — RUC 10 links to existing Persona

- **GIVEN** Persona exists with DNI "12345678" (mapped to RUC 10 "10123456789" via SUNAT API)
- **WHEN** POST `/api/v1/entidad/empresa` with RUC "10123456789" (RUC 10, auto-detected from prefix)
- **THEN** the response is 201
- **AND** `personaId` links to the matching persona
- **AND** `razonSocial` is omitted (uses Persona's `nombreCompleto` for display)

#### Scenario: ENT-001-5 — RUC 10 without matching Persona

- **GIVEN** no Persona exists with DNI matching the RUC 10 prefix
- **WHEN** POST empresa with RUC "10123456789"
- **THEN** the empresa is created without `personaId`
- **AND** `rol` = CLIENTE
- **AND** `razonSocial` remains empty (SUNAT RUC 10 only returns nombre via `apenomdenunciado`)

### Requirement: ENT-002 — Automatic role detection

- **Priority**: MUST
- **Description**: The Empresa's `rol` (CLIENTE/PROVEEDOR/AMBOS) is NEVER selected manually. It is detected and promoted by usage context:
  - When an Empresa appears as the **client/payer** in a Factura (caja-comprobante) → role includes CLIENTE
  - When an Empresa appears as the **payee/supplier** in a purchase/payment (caja-liquidacion pago a proveedor, or future farmacia procurement) → role includes PROVEEDOR
  - When an Empresa has been used in both contexts → role becomes AMBOS
  - Initial creation defaults to CLIENTE (most common case: B2B invoice issuance)
  - Role promotion is idempotent: setting CLIENTE on an already-AMBOS empresa stays AMBOS
- **Acceptance Criteria**:
  - [ ] New empresa defaults to CLIENTE
  - [ ] First use as PROVEEDOR context promotes to AMBOS (if previously CLIENTE)
  - [ ] First use as CLIENTE context on a PROVEEDOR-only empresa promotes to AMBOS
  - [ ] Role is stored as a DB column for efficient querying (not computed at runtime)
  - [ ] Role audit log records promotions with `usuarioId`, `fecha`, `oldRol`, `newRol`

#### Scenario: ENT-002-1 — Default role is CLIENTE

- **GIVEN** a new Empresa is created without explicit rol
- **WHEN** GET `/api/v1/entidad/empresa/{id}`
- **THEN** `rol` = "CLIENTE"

#### Scenario: ENT-002-2 — Supplier use promotes to AMBOS

- **GIVEN** Empresa exists with rol=CLIENTE
- **WHEN** the system records a supplier payment referencing this Empresa (via farmacia or caja)
- **THEN** the Empresa's rol is promoted to "AMBOS"
- **AND** a role promotion audit entry is created

#### Scenario: ENT-002-3 — Already AMBOS stays AMBOS

- **GIVEN** Empresa exists with rol=AMBOS
- **WHEN** the system records another client invoice
- **THEN** rol remains "AMBOS" (no change)

### Requirement: ENT-003 — SUNAT RUC API integration

- **Priority**: MUST
- **Description**: The system SHALL integrate with SUNAT's public RUC query endpoint to auto-fill Empresa data. The endpoint is: `https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm?accion=obtenerDatosRuc&nroRuc={ruc}`. The response parsing differs by RUC type:
  - **RUC 20** (persona jurídica): Returns full data — `razonSocial` (from `rsoSocial` or `desRazonSocial`), `direccionFiscal` (from `direccionCompleta`), `ubigeo` (from `codUbigeo`). These are stored in the Empresa record.
  - **RUC 10** (persona natural): Returns only `apenomdenunciado` (full name of the natural person). No dirección, no ubigeo. The system stores this as `razonSocial` (the full name from SUNAT as the display name).

  The API call is async-optional: the cashier can enter Empresa data manually without calling SUNAT. When the SUNAT call is made, returned data pre-fills fields but the cashier can override them before saving. SUNAT API errors (timeout, connection failure) are non-blocking — the form remains editable and the cashier can submit manually.

  All SUNAT queries are logged in a `sunat_consulta_log` table: `ruc`, `fecha`, `ipOrigen`, `usuarioId`, `respuestaRaw` (truncated to 2000 chars), `exito` (boolean).

  This is the EXISTING production SUNAT endpoint. No API key or authentication required.
- **Acceptance Criteria**:
  - [ ] GET `/api/v1/entidad/sunat/consultar/{ruc}` returns SUNAT data (or fallback to manual form)
  - [ ] RUC 20 returns razonSocial + direccionFiscal + ubigeo from SUNAT
  - [ ] RUC 10 returns `apenomdenunciado` as the display name
  - [ ] SUNAT timeout does not block the user — manual entry remains available
  - [ ] Every SUNAT query is logged in `sunat_consulta_log`
  - [ ] The SUNAT API client has configurable timeout (default 10s) via application properties

#### Scenario: ENT-003-1 — Consult RUC 20 successfully

- **GIVEN** RUC "20123456789" (RUC 20)
- **WHEN** GET `/api/v1/entidad/sunat/consultar/20123456789`
- **THEN** the response includes `razonSocial`, `direccionFiscal`, and `ubigeo` from SUNAT
- **AND** the query is logged with `exito: true`

#### Scenario: ENT-003-2 — Consult RUC 10 successfully

- **GIVEN** RUC "10123456789" (RUC 10, persona natural)
- **WHEN** GET `/api/v1/entidad/sunat/consultar/10123456789`
- **THEN** the response includes `nombreCompleto` (from `apenomdenunciado`)
- **AND** `direccionFiscal` and `ubigeo` are empty (API does not return them for RUC 10)
- **AND** the query is logged

#### Scenario: ENT-003-3 — SUNAT API timeout

- **GIVEN** SUNAT API is unreachable or times out
- **WHEN** GET `/api/v1/entidad/sunat/consultar/20123456789`
- **THEN** the response is 503 with a message indicating SUNAT unavailable
- **AND** the user can still create the Empresa manually via POST (no RUC pre-fill)
- **AND** the query is logged with `exito: false`

#### Scenario: ENT-003-4 — SUNAT call on Empresa creation

- **GIVEN** the cashier enters RUC "20123456789" on the creation form and clicks "Consultar SUNAT"
- **WHEN** the SUNAT API returns data
- **THEN** the form fields are pre-filled with `razonSocial`, `direccionFiscal`, `ubigeo`
- **AND** the cashier can edit the pre-filled data before submitting
- **AND** the final POST creates the Empresa with any manual overrides

### Requirement: ENT-004 — CRUD Empresa

- **Priority**: MUST
- **Description**: Full CRUD for Empresa entities. List with search/filter by RUC, razón social, rol, estado. GET by RUC (primary key) and by ID (internal PK). Update allows modifying non-RUC fields. Soft delete (set estado=INACTIVO, never hard delete). Pagination for list endpoints.
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/entidad/empresa` creates empresa (with or without SUNAT pre-fill)
  - [ ] GET `/api/v1/entidad/empresa/{id}` by internal ID
  - [ ] GET `/api/v1/entidad/empresa/ruc/{ruc}` by RUC
  - [ ] GET `/api/v1/entidad/empresa` with pagination, filter by `?rol=PROVEEDOR&estado=ACTIVO&q=texto`
  - [ ] PUT `/api/v1/entidad/empresa/{id}` updates editable fields (not RUC)
  - [ ] DELETE sets estado=INACTIVO (soft delete)

#### Scenario: ENT-004-1 — Search by RUC or name

- **GIVEN** 100 empresas exist
- **WHEN** GET `/api/v1/entidad/empresa?q=clínica&page=0&size=20`
- **THEN** the response returns paginated results matching "clínica" in RUC or razón social
- **AND** `totalElements` shows the total count

#### Scenario: ENT-004-2 — Soft delete prevents reuse in transactions

- **GIVEN** Empresa "20123456789" has estado=INACTIVO
- **WHEN** the cashier tries to select this Empresa for a new Factura
- **THEN** the empresa does not appear in client lookup results
- **AND** attempting to reference it directly returns 404

### Requirement: ENT-005 — Persona vinculación for RUC 10

- **Priority**: MUST
- **Description**: When creating a RUC 10 Empresa (persona natural), the system attempts to match the RUC's underlying DNI (digits 2–11 of the RUC) to an existing Persona record. If a match is found, `personaId` is auto-linked. The cashier MAY also manually link or unlink a Persona. The link enables: (1) displaying the Persona's full name for the business name, (2) using the Persona's address if no SUNAT address returned, (3) cross-referencing between clinical patient data and billing data. The link does NOT create a Persona if one doesn't exist — the match is a convenience, not a requirement.
- **Acceptance Criteria**:
  - [ ] RUC 10 creation auto-searches for Persona by DNI derived from RUC digits 2-11
  - [ ] Auto-link is silent (no error if not found)
  - [ ] Manual link/unlink via PUT with `personaId`
  - [ ] Persona soft-delete does NOT cascade to Empresa
  - [ ] Persona DNI change does NOT affect Empresa (RUC stays unchanged)

#### Scenario: ENT-005-1 — Auto-link on RUC 10 creation

- **GIVEN** Persona exists with DNI "12345678"
- **WHEN** POST `/api/v1/entidad/empresa` with RUC "10123456789" (digits 2-11 = "12345678")
- **THEN** `personaId` is auto-set to the matching Persona's ID
- **AND** the response includes the linked Persona data in a `persona` embedded field

#### Scenario: ENT-005-2 — No Persona match

- **GIVEN** no Persona exists with DNI matching RUC digits 2-11
- **WHEN** POST same RUC 10
- **THEN** the empresa is created with `personaId = null`
- **AND** `razonSocial` is empty (SUNAT `apenomdenunciado` not yet fetched)

### Requirement: ENT-006 — Empresa usage in Caja Comprobante

- **Priority**: MUST
- **Description**: When issuing a Factura (tipo 01), the Compobante references an `empresaId` (FK to Empresa) rather than storing inline RUC/razón social. The comprobante denormalizes `clienteRuc`, `clienteRazonSocial`, `clienteDireccion` at issuance time for SUNAT XML immutability, but these values come FROM the Empresa record. For Boleta (tipo 03), the client is a Persona (DNI), not an Empresa. The cashier selects: (1) Persona for Boleta, (2) Empresa for Factura. The selection is mutually exclusive.
- **Acceptance Criteria**:
  - [ ] Factura requires `empresaId` (not `personaId`)
  - [ ] Boleta requires `personaId` (not `empresaId`)
  - [ ] At issuance, comprobante copies RUC, razón social, dirección from Empresa
  - [ ] If Empresa changes later, existing comprobantes keep the original data (denormalized)
  - [ ] Empresa lookup in comprobante form filters by ACTIVO and includes search by RUC/razón social

#### Scenario: ENT-006-1 — Select Empresa for Factura

- **GIVEN** Empresa exists with RUC "20123456789", razónSocial "Clínica Ejemplo SAC"
- **WHEN** POST `/api/v1/caja/comprobante/{liquidacionId}/emitir` with `{ tipoComprobante: "01", empresaId: 5 }`
- **THEN** the response is 201
- **AND** `clienteRuc` = "20123456789", `clienteRazonSocial` = "Clínica Ejemplo SAC", `clienteDireccion` = stored dirección
- **AND** `empresaId` references the Empresa record

#### Scenario: ENT-006-2 — Factura without empresaId rejected

- **GIVEN** tipoComprobante = "01" (Factura)
- **WHEN** POST emitir without `empresaId`
- **THEN** the response is 400 — Factura requires an Empresa with RUC

### Requirement: ENT-007 — Permissions

- **Priority**: MUST
- **Description**: Empresa operations use `entidad:{accion}` pattern: `entidad:crear` for POST, `entidad:ver` for GET, `entidad:editar` for PUT. SUNAT consultation uses `entidad:consultar-sunat`. These permissions are seeded in the DataInitializer alongside `caja:*` permissions. CAJA, ADMIN, and FARMACIA roles have access.
- **Acceptance Criteria**:
  - [ ] Usuario with CAJA role can create and search Empresas
  - [ ] Usuario with FARMACIA role can search Empresas (read-only)
  - [ ] Usuario without asignación receives 403

#### Scenario: ENT-007-1 — CAJA can create Empresa

- **GIVEN** a Usuario with role CAJA
- **WHEN** POST `/api/v1/entidad/empresa`
- **THEN** the response is 201

#### Scenario: ENT-007-2 — MEDICO cannot access Empresa

- **GIVEN** a Usuario with role MEDICO
- **WHEN** GET `/api/v1/entidad/empresa`
- **THEN** the response is 403 Forbidden

### Requirement: ENT-008 — Data privacy

- **Priority**: MUST
- **Description**: RUC and razón social are business-identifying information, not PII per Law 29733. However, for RUC 10 (persona natural), the RUC indirectly identifies an individual. The `sunat_consulta_log.respuestaRaw` field MAY contain PII (full name from `apenomdenunciado`) and MUST be excluded from application logs. Empresa list endpoints MUST NOT expose `sunat_consulta_log` data.
- **Acceptance Criteria**:
  - [ ] `respuestaRaw` in sunat_consulta_log is excluded from toString() and logs
  - [ ] Empresa list endpoint does not expose SUNAT raw response data
