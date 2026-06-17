# Delta for Autenticación

## MODIFIED Requirements

### Requirement: R-001 — Browser session via formLogin

The system MUST enable Spring Security `formLogin()` for requests not under `/api/**`. Credentials MUST be validated against `tb_usuarios` with BCrypt. Sessions MUST store the `SecurityContext`.

On successful login, if the Usuario has `passwordChangeRequired=true`, the system MUST redirect to `/cambiar-contrasena` instead of the application home page. The password change template MUST be accessible from the portal-seguridad sidebar or profile menu.
(Previously: Always redirected to home after successful login)

#### Scenario: SC-001-1 — Browser login success
- GIVEN a valid Usuario with BCrypt password and `passwordChangeRequired=false`
- WHEN POST `/login` with correct username and password
- THEN the system creates an HTTP session with authenticated SecurityContext
- AND redirects to the application home page

#### Scenario: SC-001-1-B — Login with password change required
- GIVEN a valid Usuario with BCrypt password and `passwordChangeRequired=true`
- WHEN POST `/login` with correct username and password
- THEN the system creates an HTTP session with authenticated SecurityContext
- AND redirects to `/cambiar-contrasena`

#### Scenario: SC-001-2 — Invalid credentials
- GIVEN a valid Usuario
- WHEN POST `/login` with incorrect password
- THEN the system returns 401 and shows the login form with error message
