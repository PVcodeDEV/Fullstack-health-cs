# Proposal: Frontend Portales

## Intent

The current single-layout Thymeleaf frontend (`layouts/main.html` + shared header/footer) does not scale for a multi-module ERP. All users see the same navigation and UI regardless of role, causing:

- **Security exposure**: Users see modules they have no permission for (e.g., a pharmacist seeing Caja liquidaciones)
- **Cognitive overload**: Single navigation mixes Admisión, Farmacia, Caja, RRHH sections
- **Maintenance burden**: One layout file grows uncontrollably as modules are added
- **Missing auth UX**: No login page exists (SecurityConfig expects `/login`); no dashboard handler redirects users post-login

This change transforms the frontend into **4 dedicated portals** with isolated layouts, navigation, and theming — each showing only what the user's role permits.

## Scope

### In Scope
- **Login template** (`templates/login.html`) — form login page with Tailwind styling
- **Custom AuthenticationSuccessHandler** — redirects users to their portal based on roles/permissions
- **4 Portal directories** under `templates/`:
  - `portal-asistencial/` (Admisión + Médicos: Pacientes, Admisiones, HCE, SOP — Blue theme)
  - `portal-farmacia/` (Farmacéuticos: Despacho recetas, Stock, Mermas — Green theme)
  - `portal-caja/` (Cajeros + Contabilidad: Liquidaciones, Comprobantes, Sesión caja — Teal theme)
  - `portal-administrativo/` (Admin + RRHH + Gerencia: RRHH, Maestros, Usuarios/Roles — Slate theme)
- **Base layout** (`layouts/base.html`) — shared boilerplate (meta, CSS, footer, scripts)
- **Per-portal fragments**: `fragments/header.html`, `fragments/sidebar.html`, `fragments/nav.html`
- **Portal controllers**: `AsistencialPortalController`, `FarmaciaPortalController`, `CajaPortalController`, `AdministrativoPortalController`
- **New permission codes**: `asistencial:ver`, `farmacia:ver`, `caja:ver`, `administrativo:ver`
- **SecurityConfig updates**: Remove default success URL, add custom handler, per-portal `@PreAuthorize`
- **Existing controllers** updated to use portal layouts via `layout:decorate`
- **Tailwind config**: CSS variables per portal (`--portal-primary`, `--portal-secondary`)

### Out of Scope
- Backend business logic changes (entities, services, repositories)
- SUNAT integration (already in modulo-caja)
- Reportes module (future: Portal Administrativo placeholder)
- Mobile-responsive breakpoints beyond Tailwind defaults
- API-only endpoints (JWT auth unchanged)
- Module-specific templates inside each portal (added in module-specific changes)

## Capabilities

### New Capabilities
- `portal-asistencial`: Assistencia portal layout, navigation, dashboard, Admisión/Médicos sections
- `portal-farmacia`: Farmacia portal layout, navigation, dashboard, Despacho/Stock/Mermas sections
- `portal-caja`: Caja portal layout, navigation, dashboard, Liquidaciones/Comprobantes/Sesión sections
- `portal-administrativo`: Admin portal layout, navigation, dashboard, RRHH/Maestros/Usuarios sections
- `portal-authentication`: Login template + AuthenticationSuccessHandler + portal permission codes

### Modified Capabilities
- `seguridad-autenticacion`: Update SecurityConfig to use custom success handler, remove defaultSuccessUrl
- `caja-liquidacion`: Update Caja controllers to decorate `portal-caja/layouts/portal.html`
- `clinica-admision`: Update Admisión controllers to decorate `portal-asistencial/layouts/portal.html`
- `clinica-hospitalizacion`: Update Hospitalización controllers to decorate `portal-asistencial/layouts/portal.html`
- `clinica-sop`: Update SOP controllers to decorate `portal-asistencial/layouts/portal.html`
- `farmacia-core-v1`: Update Farmacia controllers to decorate `portal-farmacia/layouts/portal.html`
- `rrhh-base`: Update RRHH controllers to decorate `portal-administrativo/layouts/portal.html`

## Approach

**Template Organization**
```
templates/
  login.html
  layouts/base.html                    ← shared boilerplate
  portal-asistencial/
    layouts/portal.html                ← extends base, defines --portal-primary: #3B82F6
    fragments/header.html, sidebar.html, nav.html
    dashboard.html
    pacientes/, admisiones/, hce/, sop/
  portal-farmacia/
    layouts/portal.html                ← extends base, defines --portal-primary: #22C55E
    fragments/header.html, sidebar.html, nav.html
    dashboard.html
    despacho/, stock/, mermas/
  portal-caja/
    layouts/portal.html                ← extends base, defines --portal-primary: #14B8A6
    fragments/header.html, sidebar.html, nav.html
    dashboard.html
    liquidacion/, comprobante/, sesion/
  portal-administrativo/
    layouts/portal.html                ← extends base, defines --portal-primary: #64748B
    fragments/header.html, sidebar.html, nav.html
    dashboard.html
    rrhh/, maestros/, usuarios/
```

**Layout Strategy**
- `layouts/base.html` — `<html>`, `<head>` (meta, Tailwind output.css, HTMX), `<body>`; `<footer>`; common scripts
- Each `portal-*/layouts/portal.html` — `layout:decorate="~{layouts/base}"`; defines CSS variables; provides `fragments/header`, `fragments/sidebar`, `fragments/nav` overrides
- Portal-specific templates — `layout:decorate="~{portal-*/layouts/portal}"`

**Theming**
- CSS variables in each portal's `portal.html`: `--portal-primary`, `--portal-secondary`, `--portal-primary-hover`
- Templates use `bg-[var(--portal-primary)]`, `text-[var(--portal-primary)]`, `hover:bg-[var(--portal-primary-hover)]`
- Tailwind safelist ensures variables compile

**Navigation & RBAC**
- **ADMIN user** (has `ROLE_ADMIN` + all permissions) → sees portal selector in header, can navigate all 4 portals
- **Other roles** → custom `AuthenticationSuccessHandler` reads authorities, redirects to single authorized portal:
  - `asistencial:ver` → `/asistencial/dashboard`
  - `farmacia:ver` → `/farmacia/dashboard`
  - `caja:ver` → `/caja/dashboard`
  - `administrativo:ver` → `/administrativo/dashboard`
  - Fallback: `/login?error=unauthorized`
- Per-portal nav fragments are **static** (hardcoded for that portal's sections)
- Fine-grained items within portal use Thymeleaf Security: `sec:authorize="hasAuthority('caja:aprobar')"`

**Routing**
| Path | Handler |
|------|---------|
| `GET /login` | `LoginController` → `login.html` |
| `POST /login` | Spring Security form login |
| `/asistencial/**` | `AsistencialPortalController` + module controllers |
| `/farmacia/**` | `FarmaciaPortalController` + module controllers |
| `/caja/**` | `CajaPortalController` + module controllers |
| `/administrativo/**` | `AdministrativoPortalController` + module controllers |

**Security Config Changes**
- Remove `.defaultSuccessUrl("/dashboard")`
- Add `.successHandler(portalAuthenticationSuccessHandler())`
- Portal controllers annotated: `@PreAuthorize("hasAnyAuthority('caja:ver', 'ROLE_ADMIN')")` etc.

**Implementation Order**
1. **Phase 0 (Blocker)**: Login template + `PortalAuthenticationSuccessHandler` + permission seeds
2. **Phase 1**: Portal Caja (lowest risk — existing Caja templates prove the pattern)
3. **Phase 2**: Portal Asistencial
4. **Phase 3**: Portal Farmacia
5. **Phase 4**: Portal Administrativo

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `templates/login.html` | New | Login page with form, error handling, Tailwind styling |
| `templates/layouts/base.html` | New | Shared boilerplate layout |
| `templates/portal-*/layouts/portal.html` | New (4) | Per-portal layout extending base, defines CSS variables |
| `templates/portal-*/fragments/*.html` | New (12) | Header, sidebar, nav per portal |
| `templates/portal-*/dashboard.html` | New (4) | Portal entry dashboards |
| `com.clinica.web.portal.*` | New (4) | Portal controllers |
| `com.clinica.web.auth.PortalAuthenticationSuccessHandler` | New | Redirects to portal based on authorities |
| `com.clinica.config.SecurityConfig` | Modified | Custom success handler, remove defaultSuccessUrl |
| Existing module controllers | Modified | Add `layout:decorate` to portal layouts |
| `backend/frontend/tailwind.config.js` | Modified | Safelist portal CSS variables |
| `seguridad` DataInitializer | Modified | Seed `asistencial:ver`, `farmacia:ver`, `caja:ver`, `administrativo:ver` |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Login loop / redirect loop | High | Handler validates target portal permission before redirect; unit test all role combinations |
| ADMIN loses cross-portal access | Medium | Explicit `ROLE_ADMIN` check in handler; portal selector only renders for ADMIN |
| CSS variable bleed between portals | Low | Scoped CSS variables per portal layout; no global overrides |
| Template duplication (headers/sidebars) | Medium | Base layout + fragment overrides keep DRY; shared components in `fragments/common/` if needed |
| Existing Caja templates break | Medium | Phase 1 targets Caja first; incremental migration with layout:decorate |
| Missing permission seeds in prod | Medium | DataInitializer seeds all 4 permissions; idempotent on restart |
| HTMX partial renders lose layout context | Low | Portal controllers return full page; fragments loaded via HTMX use same layout |

## Rollback Plan

1. Revert SecurityConfig to `.defaultSuccessUrl("/dashboard")`, remove custom handler bean
2. Delete `templates/login.html`, `templates/layouts/base.html`, `templates/portal-*/`
3. Revert module controllers to `layout:decorate="~{layouts/main}"`
4. Revert Tailwind config safelist changes
5. Remove 4 portal permissions from DataInitializer (or keep — harmless)
6. Re-deploy; verify original `layouts/main.html` works

## Dependencies

- `modulo-seguridad` — `ROLE_ADMIN`, permission infrastructure, DataInitializer
- `modulo-caja` — Existing Caja templates to migrate first (proves pattern)
- `clinica-admision/hospitalizacion/sop` — Controllers updated in Phase 2
- `farmacia-core-v1` — Controllers updated in Phase 3
- `rrhh-base` — Controllers updated in Phase 4
- `frontend` build — `bun run build` compiles Tailwind with new safelist

## Success Criteria

- [ ] `GET /login` renders styled login page, `POST /login` authenticates
- [ ] User with `caja:ver` → lands on `/caja/dashboard` after login
- [ ] User with `asistencial:ver` → lands on `/asistencial/dashboard`
- [ ] User with `farmacia:ver` → lands on `/farmacia/dashboard`
- [ ] User with `administrativo:ver` → lands on `/administrativo/dashboard`
- [ ] ADMIN user sees portal selector, can navigate all 4 portals
- [ ] Each portal shows correct theme color (Blue/Green/Teal/Slate) in header/sidebar
- [ ] Caja existing templates (liquidación, comprobante, sesión) render inside Portal Caja layout
- [ ] No 403/404 on portal entry points for authorized roles
- [ ] Unauthorized role accessing `/caja/**` → 403 (not redirect loop)

## Delivery

### Phase 0: Auth Foundation (Blocker)
- Login template + `PortalAuthenticationSuccessHandler`
- Seed 4 portal permissions in DataInitializer
- SecurityConfig: custom success handler, remove defaultSuccessUrl
- **Gate**: Login works, redirects by role verified in test

### Phase 1: Portal Caja
- `templates/portal-caja/` structure, layout, fragments, dashboard
- `CajaPortalController` + `@PreAuthorize("hasAnyAuthority('caja:ver','ROLE_ADMIN')")`
- Migrate existing Caja templates to `layout:decorate="~{portal-caja/layouts/portal}"`
- **Gate**: Cashier flows work end-to-end in new portal

### Phase 2: Portal Asistencial
- `templates/portal-asistencial/` structure
- `AsistencialPortalController` + `@PreAuthorize("hasAnyAuthority('asistencial:ver','ROLE_ADMIN')")`
- Migrate Admisión/Hospitalización/SOP controllers
- **Gate**: Admisión + Médico workflows work

### Phase 3: Portal Farmacia
- `templates/portal-farmacia/` structure
- `FarmaciaPortalController` + `@PreAuthorize("hasAnyAuthority('farmacia:ver','ROLE_ADMIN')")`
- Migrate Farmacia controllers
- **Gate**: Despacho/Stock/Mermas work

### Phase 4: Portal Administrativo
- `templates/portal-administrativo/` structure
- `AdministrativoPortalController` + `@PreAuthorize("hasAnyAuthority('administrativo:ver','ROLE_ADMIN')")`
- Migrate RRHH/Maestros/Usuarios controllers
- Portal selector in header for ADMIN
- **Gate**: Admin navigates all portals; RRHH/Gerencia workflows work