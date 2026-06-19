# Proposal: RRHH Portal Trabajador & Contrato

## Intent

Build the Thymeleaf + HTMX portal UI for **Trabajador** and **Contrato** under `/administrativo/rrhh`. Backend REST APIs, entities, services, and tests are complete. Only MVC portal layer missing.

## Scope

### In Scope
- Portal MVC controllers: `TrabajadorPortalController`, `ContratoPortalController` in `com.clinica.rrhh.controller`
- Thymeleaf templates: list/create/edit/view with HTMX modals, dynamic tables, form submissions
- Sidebar: add "Trabajadores" and "Contratos" links under `/administrativo/rrhh`
- PreAuthorize: `hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')` reads, `hasAnyAuthority('administrativo:editar', 'ROLE_ADMIN')` writes
- HTMX patterns matching `portal-seguridad` spec

### Out of Scope
- Backend API changes (complete)
- New REST endpoints/DTOs
- Business logic modifications
- Medico/Persona portal
- Reporting/exports

## Capabilities

### New Capabilities
- `rrhh-trabajador-portal`: Portal CRUD — list with filters, create/edit modal, view detail, reingreso, contratos/periodos sub-tabs
- `rrhh-contrato-portal`: Portal CRUD — list by trabajador, create/edit modal, view detail, resolver/suspender/reactivar, estado badge

### Modified Capabilities
- None

## Approach

Follow `portal-seguridad` pattern:
1. `@Controller` classes mapping to `/administrativo/rrhh/trabajadores` and `/administrativo/rrhh/contratos`
2. Controllers call existing service layer directly (`TrabajadorService`, `ContratoService`, etc.) — no HTTP overhead, same JVM
3. Templates under `templates/portal-administrativo/rrhh/trabajadores/` and `templates/portal-administrativo/rrhh/contratos/`
4. Reuse `portal.html` layout and fragments
5. HTMX for modals (`hx-get`/`hx-post`), inline actions, toast notifications

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `src/main/java/com/clinica/rrhh/controller/TrabajadorPortalController.java` | New | Portal MVC controller |
| `src/main/java/com/clinica/rrhh/controller/ContratoPortalController.java` | New | Portal MVC controller |
| `templates/portal-administrativo/rrhh/trabajadores/` | New | List, form, detail, modals |
| `templates/portal-administrativo/rrhh/contratos/` | New | List, form, detail, modals |
| `templates/portal-administrativo/fragments/sidebar.html` | Modified | Add RRHH sub-links |
| `templates/portal-administrativo/layouts/portal.html` | None | Reuse layout |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| HTMX validation UX inconsistent with portal-seguridad | Medium | Reuse validation/error fragments from security portal |
| Portal (`administrativo:*`) vs API (`rrhh:*`) permission mismatch | Low | Verify authority mapping; align on `administrativo:ver/editar` |
| Controller→REST latency | Low | Use service-layer calls; cache persona lookups |

## Rollback Plan

1. Delete two new controller classes
2. Delete two template directories
3. Revert `sidebar.html` RRHH sub-links
4. No DB changes — pure frontend rollback

## Dependencies

- `portal-seguridad` as reference pattern
- Backend APIs at `/api/v1/trabajadores` and `/api/v1/contratos` deployed

## Success Criteria

- [ ] `/administrativo/rrhh/trabajadores` renders list with HTMX CRUD
- [ ] `/administrativo/rrhh/contratos` renders list with HTMX CRUD + resolver
- [ ] Sidebar active highlight on both pages
- [ ] PreAuthorize enforces `administrativo:ver/editar`
- [ ] Inline validation errors matching security portal UX