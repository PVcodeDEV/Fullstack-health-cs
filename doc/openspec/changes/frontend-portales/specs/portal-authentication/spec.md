# Spec: Portal Authentication

## Change
frontend-portales / Phase 1

## Requirements

### LOG-001: Login Page
**Phase**: 1
**Description**: A login page at `GET /login` with username/password form.
- Form fields: username (text), password (password), submit button
- Error message display for invalid credentials
- Links: "¿Olvidó su contraseña?" (placeholder, no-op)
- Uses Spring Security form login (`POST /login`)
- Styled with Tailwind, centered card layout, no portal chrome (nav/sidebar)

### LOG-002: Post-Login Redirect
**Phase**: 1
**Description**: Custom `AuthenticationSuccessHandler` that redirects based on user's highest-priority authority.
- Priority order: `ROLE_ADMIN` > `asistencial:ver` > `farmacia:ver` > `caja:ver` > `administrativo:ver`
- ADMIN → redirects to `/administrativo` (can navigate to all portals)
- Specific portal → redirects to that portal's entry
- Falls back to `/administrativo` if no portal permission found

### LOG-003: ADMIN Sees All
**Phase**: 1
**Description**: User with `ROLE_ADMIN` authority sees navigation links to all 4 portals.
- Admin dashboard shows tiles/links for each portal
- Each portal's layout shows an "admin bar" or switcher for ADMIN users

### LOG-004: Logout
**Phase**: 1
**Description**: `GET /logout` triggers Spring Security logout, redirects to `/login?logout`.
- Shows "Sesión cerrada correctamente" message on login page

### LOG-005: Unauthenticated Redirect
**Phase**: 1
**Description**: Any request to a portal URL without authentication redirects to `/login`.
- Uses existing Spring Security configuration (`authenticated()` requests)
- Preserves the original URL as a query param for post-login redirect

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| LOG-001-1 | Login success valid credentials | User has valid credentials | POST /login with correct username/password | Redirect to user's portal |
| LOG-001-2 | Login failure invalid credentials | User exists, wrong password | POST /login with wrong password | Show error, stay on /login |
| LOG-001-3 | Login unknown user | User doesn't exist | POST /login with unknown username | Show error, stay on /login |
| LOG-002-1 | Admin redirect | User has ROLE_ADMIN + asistencial:ver | POST /login with admin credentials | Redirect to /administrativo |
| LOG-002-2 | Caja redirect | User has caja:ver only | POST /login with caja credentials | Redirect to /caja |
| LOG-002-3 | Asistencial redirect | User has asistencial:ver only | POST /login with asistencial credentials | Redirect to /asistencial |
| LOG-003-1 | Admin sees all portals | User has ROLE_ADMIN | View any portal layout | Navigation links to all 4 portals visible |
| LOG-004-1 | Logout | User is authenticated | GET /logout | Redirect to /login?logout with success message |
| LOG-005-1 | Unauthenticated access | No session | GET /caja | Redirect to /login with return URL |
