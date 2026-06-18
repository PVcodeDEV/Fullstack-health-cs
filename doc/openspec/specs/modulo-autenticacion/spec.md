# Autenticación Specification

## Purpose

Dual authentication: formLogin with HTTP sessions for browser access, and JWT bearer tokens for stateless API access. Provides JWT issuance endpoint and configurable token lifetime.

## Requirements

### Requirement: R-001 — Browser session via formLogin

The system MUST enable Spring Security `formLogin()` for requests not under `/api/**`. Credentials MUST be validated against `tb_usuarios` with BCrypt. Sessions MUST store the `SecurityContext`.

#### Scenario: SC-001-1 — Browser login success
- GIVEN a valid Usuario with BCrypt password
- WHEN POST `/login` with correct username and password
- THEN the system creates an HTTP session with authenticated SecurityContext
- AND redirects to the application home page

#### Scenario: SC-001-2 — Invalid credentials
- GIVEN a valid Usuario
- WHEN POST `/login` with incorrect password
- THEN the system returns 401 and shows the login form with error message

### Requirement: R-002 — JWT auth for REST API

The system MUST configure `oauth2ResourceServer.jwt()` for all `/api/**` endpoints. JWT tokens MUST be validated statelessly via Nimbus JOSE + JWT. No HTTP session SHALL be created for API requests.

#### Scenario: SC-002-1 — Valid JWT grants access
- GIVEN a valid non-expired JWT
- WHEN GET `/api/v1/personas` with `Authorization: Bearer {jwt}`
- THEN the system extracts the SecurityContext and returns 200

#### Scenario: SC-002-2 — Missing token returns 401
- GIVEN no Authorization header
- WHEN GET `/api/v1/personas`
- THEN the system returns 401 Unauthorized

### Requirement: R-003 — JWT issuance endpoint

The system MUST expose `POST /api/v1/auth/login` accepting `{ "username": "...", "password": "..." }`. On success, it MUST return a signed JWT with `sub` (username), `roles` (role names), and `exp` claims.

#### Scenario: SC-003-1 — Successful login
- GIVEN a valid Usuario with role ADMIN
- WHEN POST `/api/v1/auth/login` with correct credentials
- THEN the system returns 200 with `{ "token": "{jwt}", "type": "Bearer", "expiresIn": 3600 }`

#### Scenario: SC-003-2 — Invalid credentials
- GIVEN a valid Usuario
- WHEN POST `/api/v1/auth/login` with wrong password
- THEN the system returns 401 with `{ "error": "Credenciales inválidas" }`

### Requirement: R-004 — Session timeout

Browser HTTP sessions MUST expire after 30 minutes of inactivity. On expiration, the next request MUST redirect to `/login`.

#### Scenario: SC-004-1 — Expired session redirects
- GIVEN an authenticated browser session
- WHEN no request is made for 30+ minutes
- THEN the next request redirects to `/login`

### Requirement: R-005 — Usuarios vinculados a Trabajadores (regla de dominio)

El sistema DEBE exigir que todo usuario (excepto el administrador de bootstrap) esté vinculado a un trabajador activo registrado en el módulo RRHH. La entidad `tb_usuarios.usu_trabajador_id` referencia a `tb_trabajadores`.

- El administrador del sistema (usuario de bootstrap, DNI: 72852927) es la ÚNICA excepción — puede existir sin trabajador asociado para permitir la configuración inicial del sistema.
- Todo usuario creado posteriormente DEBE seleccionar o crear un trabajador existente.
- La creación de un usuario desde el portal de seguridad debe ofrecer la opción de crear un trabajador sobre la marcha si la persona no es aún trabajador.

#### Scenario: SC-005-1 — Admin sin trabajador
- GIVEN la base de datos recién creada sin trabajadores
- WHEN se ejecuta el seed.sql
- THEN el usuario admin (72852927) se crea con `usu_trabajador_id = NULL`
- AND el sistema permite el login del admin sin trabajador asociado

#### Scenario: SC-005-2 — Nuevo usuario requiere trabajador
- GIVEN un usuario admin autenticado
- WHEN se crea un nuevo usuario desde el portal
- THEN el formulario requiere seleccionar una persona que YA sea trabajador
- OR permite crear un trabajador para la persona seleccionada

### Requirement: R-006 — JWT env var configuration

JWT secret and expiration MUST be configurable via env vars: `JWT_SECRET` (HMAC key, min 256-bit) and `JWT_EXPIRATION` (seconds, default 3600). Missing `JWT_SECRET` MUST fail on startup.

#### Scenario: SC-006-1 — Default expiration
- GIVEN `JWT_EXPIRATION` is not set
- WHEN a JWT is issued
- THEN `exp` claim is 3600 seconds from issuance

#### Scenario: SC-006-2 — Missing secret fails startup
- GIVEN `JWT_SECRET` is not set and no dev fallback configured
- WHEN the application starts
- THEN startup fails with a descriptive error
