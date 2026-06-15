# Spec: Caja Sesión

## Description

Cash register session management for the clinical cashier. Each cashier opens a session at the start of their shift and closes it at the end, capturing the opening and closing cash amounts. The session tracks all liquidaciones processed within it and enforces that payments can only be recorded against an open session. SesionCaja for clinical caja is independent from Farmacia's SesionCaja (which uses Series 004 and lives in `farmacia.caja`).

Base package: `com.clinica.caja.sesion`.

## Cross-References

- **CAJ-002** (caja-comprobante): Series 001 is used for clinical caja comprobantes issued within a session.
- **LIQ-003** (caja-liquidacion): All liquidaciones are linked to the cashier's open SesionCaja.
- **FARM-CAJ** (farmacia-core-v1): Existing farmacia SesionCaja is independent — clinical caja sessions do not interfere with pharmacy sessions.

## Requirements

### Requirement: SES-001 — Session open

- **Priority**: MUST
- **Description**: A cashier opens a cash register session with: `usuarioId` (the cashier opening), `fechaApertura` (server timestamp), `montoApertura` (initial cash amount in the drawer), `estado: ABIERTA`. Only one open session per cashier at a time. The session is identified by a system-generated ID. Sessions exist independently from any specific physical cash register (single caja clínica logical register).
- **Acceptance Criteria**:
  - [ ] POST `/api/v1/caja/sesion/abrir` with `montoApertura` returns 201
  - [ ] `fechaApertura` is set server-side (not client-provided)
  - [ ] `estado` is "ABIERTA"
  - [ ] Second open attempt by same user while session active returns 409

#### Scenario: SES-001-1 — Open new session

- **GIVEN** no open session exists for the current user
- **WHEN** POST `/api/v1/caja/sesion/abrir` with `{ montoApertura: 500.00 }`
- **THEN** the response is 201 with `estado: "ABIERTA"`, `fechaApertura` set server-side
- **AND** the session is linked to the authenticated usuario

#### Scenario: SES-001-2 — Double open rejected

- **GIVEN** the user already has an open session (estado ABIERTA)
- **WHEN** POST `/api/v1/caja/sesion/abrir`
- **THEN** the response is 409 — session already open

### Requirement: SES-002 — Session close

- **Priority**: MUST
- **Description**: The cashier closes the session with: `fechaCierre` (server timestamp), `montoCierre` (actual cash in drawer at close). The system computes: `totalVentas` (sum of all liquidaciones in the session), `diferencia` = `montoCierre − montoApertura − totalVentas`. If `diferencia` ≠ 0 (± tolerance), the session closes but flags a discrepancy. The tolerance is configurable via `tb_configuracion_api` (`sesionDiferenciaTolerancia`, default 1.00 PEN). After close, no more liquidaciones can be added to the session.
- **Acceptance Criteria**:
  - [ ] PUT `/api/v1/caja/sesion/{id}/cerrar` with `montoCierre` returns 200
  - [ ] `fechaCierre` is set server-side, `estado` becomes "CERRADA"
  - [ ] `diferencia` is computed and stored
  - [ ] Discrepancies within tolerance (±1.00) are silently accepted
  - [ ] Discrepancies outside tolerance are recorded but do not block closure
  - [ ] Liquidaciones cannot be created for a closed session

#### Scenario: SES-002-1 — Close session with matching amounts

- **GIVEN** a session with montoApertura=500.00, totalVentas=1200.00
- **WHEN** PUT `/api/v1/caja/sesion/{id}/cerrar` with `{ montoCierre: 1700.00 }`
- **THEN** `estado` becomes "CERRADA"
- **AND** `diferencia` = 0.00

#### Scenario: SES-002-2 — Close with small discrepancy (within tolerance)

- **GIVEN** montoApertura=500.00, totalVentas=1200.00
- **WHEN** PUT cerrar with `{ montoCierre: 1700.50 }`
- **THEN** `estado` becomes "CERRADA"
- **AND** `diferencia` = 0.50 (within 1.00 tolerance)

#### Scenario: SES-002-3 — Close with significant discrepancy

- **GIVEN** montoApertura=500.00, totalVentas=1200.00
- **WHEN** PUT cerrar with `{ montoCierre: 1690.00 }`
- **THEN** `estado` becomes "CERRADA"
- **AND** `diferencia` = -10.00 (outside 1.00 tolerance)
- **AND** the response includes a `discrepanciaWarning: true` flag

#### Scenario: SES-002-4 — Create liquidación for closed session

- **GIVEN** a session with estado CERRADA
- **WHEN** POST liquidacion/pagar linked to this session
- **THEN** the response is 422 — session is closed

### Requirement: SES-003 — Session query and current session

- **Priority**: MUST
- **Description**: The system MUST provide endpoints to: (1) get the current open session for the authenticated user, (2) list all sessions (filterable by date range, estado), (3) get session detail including summary (montoApertura, montoCierre, totalVentas, diferencia, liquidacionCount). Session data is not PII and may be cached.
- **Acceptance Criteria**:
  - [ ] GET `/api/v1/caja/sesion/actual` returns the current open session or 404
  - [ ] GET `/api/v1/caja/sesion` returns paginated session list
  - [ ] GET `/api/v1/caja/sesion/{id}` returns session detail with summary

#### Scenario: SES-003-1 — Get current open session

- **GIVEN** the user has an active session (estado ABIERTA)
- **WHEN** GET `/api/v1/caja/sesion/actual`
- **THEN** the response includes the session with estado "ABIERTA"

#### Scenario: SES-003-2 — No open session

- **GIVEN** the user has no open session
- **WHEN** GET `/api/v1/caja/sesion/actual`
- **THEN** the response is 404

### Requirement: SES-004 — Session-liquidation link

- **Priority**: MUST
- **Description**: Every Liquidacion MUST be linked to an open SesionCaja. The system automatically assigns the current user's open session to each liquidación at payment time. If no open session exists, the payment endpoint returns 422. This ensures all payments are traceable to a cash session.
- **Acceptance Criteria**:
  - [ ] Liquidacion created during payment is auto-linked to the cashier's open session
  - [ ] Payment without an open session returns 422 — open a session first

#### Scenario: SES-004-1 — Payment linked to session

- **GIVEN** the user has an open session (ID: 123)
- **WHEN** POST liquidacion/pagar
- **THEN** the resulting Liquidacion has `sesionCajaId: 123`

#### Scenario: SES-004-2 — Payment without open session

- **GIVEN** the user has no open session
- **WHEN** POST liquidacion/pagar
- **THEN** the response is 422 — no open session for this user

### Requirement: SES-005 — Independence from farmacia SesionCaja

- **Priority**: MUST
- **Description**: The clinical caja SesionCaja entity lives in `com.clinica.caja.sesion`. The existing farmacia SesionCaja lives in `com.clinica.farmacia.caja`. They have separate tables, separate permissions, and separate series (001 vs 004). Opening a clinical session does NOT interfere with pharmacy sessions and vice versa.
- **Acceptance Criteria**:
  - [ ] No import from `com.clinica.farmacia.caja` exists in `com.clinica.caja.sesion`
  - [ ] Clinical session table is `caja.sesion_caja` (or similar, separate from farmacia's)
  - [ ] Pharmacy user roles (QUIMICO, TECNICO) cannot open clinical sessions

#### Scenario: SES-005-1 — Farmacia role cannot open clinical session

- **GIVEN** a Usuario with role QUIMICO (pharmacy)
- **WHEN** POST `/api/v1/caja/sesion/abrir`
- **THEN** the response is 403 Forbidden — only CAJA role can open clinical sessions

### Requirement: SES-006 — Permissions

- **Priority**: MUST
- **Description**: SesionCaja operations use `caja:{accion}` pattern: `caja:crear` for open, `caja:editar` for close, `caja:ver` for queries. Only CAJA and ADMIN roles can open and close sessions.
- **Acceptance Criteria**:
  - [ ] Usuario with role CAJA can open, close, and query sessions
  - [ ] Usuario without CAJA or ADMIN receives 403 on open/close endpoints
  - [ ] Session list is readable by ADMIN and CAJA

#### Scenario: SES-006-1 — ADMIN can close any session

- **GIVEN** a Usuario with role ADMIN
- **WHEN** PUT cerrar on another cashier's session
- **THEN** the session is closed successfully (ADMIN override)

#### Scenario: SES-006-2 — Non-caja role cannot open session

- **GIVEN** a Usuario with role ENFERMERIA
- **WHEN** POST `/api/v1/caja/sesion/abrir`
- **THEN** the response is 403 Forbidden
