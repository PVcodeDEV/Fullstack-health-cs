# Tasks: Frontend Portales

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1,200–1,800 across 5 phases |
| 400-line budget risk | Medium |
| Chained PRs recommended | No per-phase PRs if delivered sequentially |

## Phase 1 — Auth Foundation (~350 lines) ✅

### 1.1 Create PortalAuthenticationSuccessHandler
- [x] Create `seguridad/handler/PortalAuthenticationSuccessHandler.java`
- [x] Implement priority-based redirect logic (ADMIN → asistencial → farmacia → caja → administrativo)
- [x] Fallback to `/administrativo`

### 1.2 Create base layout
- [x] Create `templates/layouts/base.html`
- [x] Header region via `th:replace` with portal fragment path from model
- [x] Sidebar region via `th:replace` with portal fragment path from model
- [x] Content via `layout:fragment="content"`
- [x] Footer hardcoded in base
- [x] CSS variable theming structure

### 1.3 Create login template
- [x] Create `templates/login.html`
- [x] Centered card layout
- [x] Username/password form fields
- [x] Error message display for invalid credentials
- [x] Logout success message display

### 1.4 Update SecurityConfig
- [x] Remove `.defaultSuccessUrl("/dashboard")`
- [x] Wire `PortalAuthenticationSuccessHandler` to form login
- [x] Set login page to `/login`
- [x] Set logout success URL to `/login?logout`

### 1.5 Seed portal permissions
- [x] Add `asistencial:ver` permiso to DataInitializer
- [x] Add `farmacia:ver` permiso to DataInitializer
- [x] Add `administrativo:ver` permiso to DataInitializer

### 1.6 Create admin portal-switcher fragment
- [x] Create `templates/fragments/admin-portal-switcher.html`
- [x] Conditionally visible via `sec:authorize="hasRole('ADMIN')"`
- [x] Links to all 4 portals

### 1.7 Verify Phase 1
- [x] `mvn compile` passes
- [x] Login page renders at `/login`
- [x] Valid credentials redirect to correct portal URL
- [x] Invalid credentials show error on `/login`
- [x] Logout redirects to `/login?logout`
- [x] Authenticated user hitting `/login` redirects to their portal

## Phase 2 — Portal Caja (~400 lines) ✅

### 2.1 Create CajaPortalController
- [x] Create `caja/controller/CajaPortalController.java`
- [x] `GET /caja` → dashboard with model data
- [x] `@PreAuthorize("hasAnyAuthority('caja:ver', 'ROLE_ADMIN')")`

### 2.2 Create Caja portal layout and fragments
- [x] Create `templates/portal-caja/layouts/portal.html`
- [x] Create `templates/portal-caja/fragments/header.html`
- [x] Create `templates/portal-caja/fragments/sidebar.html`
- [x] Teal CSS variables

### 2.3 Create Caja dashboard
- [x] Create `templates/portal-caja/dashboard.html`
- [x] Summary cards: sesión actual, últimas liquidaciones

### 2.4 Migrate existing Caja templates
- [x] Update `templates/caja/**/*.html` → `layout:decorate="~{portal-caja/layouts/portal}"`

### 2.5 Update Tailwind config
- [x] Add portal template paths to `frontend/tailwind.config.js`

### 2.6 Verify Phase 2
- [x] `mvn compile` passes
- [x] `bun run build` succeeds
- [ ] `GET /caja` renders Caja portal with theme
- [ ] Existing caja pages render with new layout

## Phase 3 — Portal Asistencial (~250 lines) ✅

### 3.1 Create AsistencialPortalController
- [x] Create controller with `GET /asistencial`

### 3.2 Create Asistencial portal layout and fragments
- [x] Portal layout, header, sidebar with blue theme

### 3.3 Create Asistencial dashboard
- [x] Dashboard template with quick access cards

### 3.4 Verify Phase 3
- [x] `GET /asistencial` renders with blue theme

## Phase 4 — Portal Farmacia (~250 lines) ✅

### 4.1 Create FarmaciaPortalController
- [x] Create controller with `GET /farmacia`

### 4.2 Create Farmacia portal layout and fragments (green theme)
- [x] Layout, header, sidebar with green theme

### 4.3 Create Farmacia dashboard
- [x] Dashboard with stock alerts, pending dispatches, transactions

### 4.4 Verify Phase 4
- [x] mvn compile passes

## Phase 5 — Portal Administrativo (~250 lines) ✅

### 5.1 Create AdministrativoPortalController
- [x] Create controller in `seguridad` module with `GET /administrativo`

### 5.2 Create Administrativo portal layout and fragments (slate theme)
- [x] Layout, header, sidebar with slate theme
- [x] Portal switcher in header for ADMIN role

### 5.3 Create Administrativo dashboard
- [x] Dashboard with cards: RRHH, Maestros, Usuarios, Seguridad

### 5.4 Verify Phase 5
- [x] mvn compile passes
