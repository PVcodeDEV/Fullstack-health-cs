# API Integration Specification

## Purpose

External identity document query layer. Provides pluggable API clients for auto-filling Persona data from SUNAT (free) and Secure providers (paid, token-based). Interfaces are mockable for tests.

## Requirements

### Requirement: R-001 — ReniecClient interface
The system MUST define a `ReniecClient` interface with method `consultaDni(String dni) → PersonaDatos` where `PersonaDatos` contains `nombres`, `apellidoPaterno`, `apellidoMaterno`, `fechaNacimiento`, `sexo`, `direccion`, `ubigeoDistrito`.

#### Scenario: SC-001-1 — Interface contract
- GIVEN a `ReniecClient` implementation
- WHEN calling `consultaDni("12345678")`
- THEN it returns a `PersonaDatos` record with all fields or throws `ApiConsultaException` on failure

### Requirement: R-002 — SunatApiClient (free)
The system MUST provide `SunatApiClient` implementing `ReniecClient`. It SHALL call `https://ww1.sunat.gob.pe/ol-ti-itfisdenreg/itfisdenreg.htm?accion=obtenerDatosDni&numDocumento={dni}` via GET with no authentication. The SHALL parse the HTML response to extract `nombres`, `apellidoPaterno`, and `apellidoMaterno`.

#### Scenario: SC-002-1 — SUNAT returns valid data
- GIVEN a valid DNI
- WHEN `SunatApiClient.consultaDni(dni)` is called
- THEN it parses the HTML and returns `PersonaDatos` with names and surnames populated, other fields null

### Requirement: R-003 — SecureApiClient (paid)
The system MUST provide `SecureApiClient` implementing `ReniecClient`. It SHALL use configurable base URL and Bearer token. It SHALL return the full `PersonaDatos` record (names, birthdate, sex, address, ubigeo).

#### Scenario: SC-003-1 — Secure API returns full data
- GIVEN a configured Secure API with valid token
- WHEN `SecureApiClient.consultaDni(dni)` is called
- THEN it returns `PersonaDatos` with all fields populated

### Requirement: R-004 — Provider fallback chain
The system MUST try `SecureApiClient` first (if enabled), fallback to `SunatApiClient` on failure. If both fail, the system MUST NOT block Persona creation — the user MUST be allowed to enter data manually.

#### Scenario: SC-004-1 — Secure fails, SUNAT succeeds
- GIVEN SecureApiClient is enabled but returns 500
- WHEN a Persona is created with DNI
- THEN the system calls `SunatApiClient` and uses its response

#### Scenario: SC-004-2 — Both APIs fail
- GIVEN both Secure and SUNAT APIs are unreachable
- WHEN a Persona is created with DNI
- THEN the system allows manual data entry for all fields
- AND the Persona is created with `fechaUltimaConsulta: null`

### Requirement: R-005 — API configuration in tb_configuracion_api

API provider configuration (base URL, token, enabled flag) SHALL be stored in `tb_configuracion_api` within the `seguridad` module. The `persona` module SHALL read them at runtime via `@ConfigurationProperties` backed by `DbApiConfigService`. Values SHALL be updatable without restart.
(Previously: API config stored as application properties in the `seguridad` module)

#### Scenario: SC-005-1 — Config read from database
- GIVEN a `tb_configuracion_api` row with `modulo=reniec`, `clave=base_url`, `valor=https://api.reniec.gob.pe/v1`
- WHEN `ReniecClient.consultaDni(dni)` is called
- THEN `base_url` is read from `tb_configuracion_api` and used for the HTTP call

#### Scenario: SC-005-2 — Runtime update without restart
- GIVEN `base_url` is `https://old.url` in the database
- WHEN the row is updated to `https://new.url`
- THEN the next API call uses `https://new.url` without application restart

### Requirement: R-006 — Graceful degradation
If `SunatApiClient` fails (network error, malformed HTML, timeout), the system MUST catch the exception, log an anonymous error (no PII), and proceed without auto-filled data.

#### Scenario: SC-006-1 — HTML parse failure
- GIVEN SUNAT returns malformed HTML
- WHEN `SunatApiClient.consultaDni(dni)` is called
- THEN it throws `ApiConsultaException` with a generic error message
- AND the caller proceeds with manual entry

### Requirement: R-007 — Anonymous failure logging
API failure logs MUST NOT include `numeroDocumento`, names, or any PII. Log only source, error type, and timestamp. Include a correlation ID for debugging.
