# Proposal: Portal Asistencial — Pacientes y Admisiones

## Intent

Build the Thymeleaf view pages for **Pacientes** (search, list, detail) and **Admisiones** (search patient → create account with surgical package → assign bed → register CIE-11 diagnosis) within the existing Portal Asistencial. Wire views to the already-implemented REST APIs in `clinica-admision`, `modulo-persona`, `maestro-cie11`, `clinica-cuenta`, and `maestro-catalogos-clinicos`.

## Scope

### In Scope
- **Pacientes view**: search by DNI/nombres, paginated results table, patient detail modal/page
- **Admisiones view**: multi-step flow — patient search → package selection → bed assignment (filtered by `tipoHabitacionId`) → CIE-11 diagnosis entry
- **Layout**: All pages extend `portal-asistencial/layouts/portal.html` with blue theme variables
- **Sidebar**: Update `#` links to real routes (`/asistencial/pacientes`, `/asistencial/admisiones`)
- **Dashboard**: Update quick-access card links from `#` to real URLs
- **Controllers**: `PacientePortalController` and `AdmisionPortalController` returning template names, setting `portalHeader`, `portalSidebar`, `activePage`, with `@PreAuthorize('admision:ver')` / `paciente:ver`
- **Templates**: New folder `portal-asistencial/pacientes/` and `portal-asistencial/admisiones/` with HTMX-powered search/filter fragments

### Out of Scope
- Hospitalización pages (separate change)
- Historia Clínica (HCE) document pages (separate change)
- SOP pages (separate change)
- Backend entities, services, repositories (already exist)
- Real dashboard statistics (placeholder data acceptable)

## Capabilities

### New Capabilities
- `portal-pacientes`: Pacientes search/list/detail views in Portal Asistencial
- `portal-admisiones`: Admisiones multi-step wizard (patient → package → bed → diagnosis)

### Modified Capabilities
- `clinica-admision`: Adds view-layer integration (existing API unchanged)
- `modulo-persona`: Adds view-layer integration (existing API unchanged)
- `maestro-cie11`: Adds autocomplete integration for diagnosis entry
- `maestro-catalogos-clinicos`: Adds room-type filtering for bed assignment

## Approach

Each module gets a **portal controller** (`*PortalController`) under `com.clinica.clinica.controller` returning Thymeleaf templates. Controllers delegate to existing service classes (`PacienteService`, `AdmisionService`, `CamaService`, `Cie11Service`, `TipoHabitacionService`). Templates live under `templates/portal-asistencial/{pacientes,admisiones}/` and use **HTMX** for interactive search, package selection, bed filtering, and diagnosis autocomplete. Sidebar and dashboard links updated to point to real controller endpoints.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `backend/src/main/java/com/clinica/clinica/controller/` | New | `PacientePortalController.java`, `AdmisionPortalController.java` |
| `backend/src/main/resources/templates/portal-asistencial/pacientes/` | New | `search.html`, `list.html`, `detail.html`, fragments |
| `backend/src/main/resources/templates/portal-asistencial/admisiones/` | New | `wizard.html`, `step-paciente.html`, `step-paquete.html`, `step-cama.html`, `step-diagnostico.html` |
| `backend/src/main/resources/templates/portal-asistencial/fragments/sidebar.html` | Modified | Replace `#` links with real routes |
| `backend/src/main/resources/templates/portal-asistencial/dashboard.html` | Modified | Update card links to real URLs |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| HTMX partial rendering conflicts with Thymeleaf layout fragments | Medium | Use `layout:fragment="content"` consistently; test each partial in isolation |
| Bed assignment filtering by `tipoHabitacionId` requires extra API call | Low | Add `GET /api/v1/camas/disponibles?tipoHabitacionId={id}` or filter client-side |
| CIE-11 autocomplete performance with ~17k codes | Low | Backend already supports ILIKE search with frequency ordering; debounce input |
| Permission mapping between portal views and API authorities | Medium | Portal controllers use same `@PreAuthorize` expressions as REST controllers |

## Rollback Plan

1. Delete `PacientePortalController` and `AdmisionPortalController`
2. Remove `templates/portal-asistencial/pacientes/` and `templates/portal-asistencial/admisiones/`
3. Revert `sidebar.html` and `dashboard.html` links to `#`
4. No database changes — zero-downtime rollback

## Dependencies

- Existing REST APIs: `clinica-admision`, `modulo-persona`, `maestro-cie11`, `clinica-cuenta`, `maestro-catalogos-clinicos`
- Thymeleaf Layout Dialect (already in project)
- HTMX (CDN or local — already available via Tailwind build)

## Success Criteria

- [ ] `/asistencial/pacientes` loads search page with DNI/nombres input and results table
- [ ] Clicking a patient shows detail with demographics and linked admissions
- [ ] `/asistencial/admisiones` loads wizard: step 1 patient search → step 2 package select → step 3 bed assign → step 4 diagnosis
- [ ] Bed dropdown filters by `tipoHabitacionId` from selected package
- [ ] CIE-11 autocomplete works on diagnosis step (code or description)
- [ ] Sidebar links navigate correctly; active page highlighted
- [ ] Dashboard cards link to real pages
- [ ] All pages respect `@PreAuthorize` — 403 for unauthorized users