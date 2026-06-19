# Design: RRHH Portal Trabajador & Contrato

## Technical Approach

Thymeleaf/HTMX portal controllers in `com.clinica.rrhh.controller` that call existing service layer directly (TrabajadorService, ContratoService). Templates follow the existing portal-seguridad pattern: list → modal create/edit → detail view with HTMX sub-tabs. Sidebar updated with RRHH sub-links.

## Architecture Decisions

### Decision: Package location

| Option | Tradeoff | Decision |
|--------|----------|----------|
| `seguridad.controller` (existing pattern) | Mixed concerns, 7+ controllers | ❌ |
| `rrhh.controller` (new sub-package) | Module-coherent, easy to extract later | ✅ |

### Decision: Service access pattern

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Call service layer directly | Same JVM, no serialization, transactional | ✅ |
| Call REST API via WebClient | HTTP overhead, serialization, auth forwarding | ❌ |

### Decision: Form submission pattern

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Modal via HTMX (hx-get/hx-post) | No page reload, inline validation, consistent with seguridad portal | ✅ |
| Dedicated form page | Simpler but more navigation, not consistent with existing portal | ❌ |

### Decision: Modals use existing layout

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Reuse `portal.html` layout + fragments | Consistent look, shared sidebar/header | ✅ |
| Standalone modals without layout | Technically simpler but inconsistent UX | ❌ |

## Data Flow

```
Browser ──GET──→ TrabajadorPortalController.list()
                  │
                  ├──→ TrabajadorService.findAll() ──→ TrabajadorRepository
                  │       └── returns List<TrabajadorResponse>
                  │
                  └──→ model.addAttribute("trabajadores", ...)
                  └──→ returns "portal-administrativo/rrhh/trabajadores/list"

Browser ──hx-get──→ TrabajadorPortalController.createForm()
                    │
                    └──→ returns "portal-administrativo/rrhh/trabajadores/form :: modal"

Browser ──hx-post──→ TrabajadorPortalController.create()
                     │
                     ├──→ TrabajadorRequest ← form params
                     ├──→ TrabajadorService.create(request)
                     └──→ HTMX response: <tr> fragment or toast
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `com/clinica/rrhh/controller/TrabajadorPortalController.java` | Create | Portal MVC: list, create, edit, detail, reingreso |
| `com/clinica/rrhh/controller/ContratoPortalController.java` | Create | Portal MVC: list, create, edit, resolver, suspender, reactivar |
| `templates/portal-administrativo/rrhh/trabajadores/list.html` | Create | List with filters, HTMX table body |
| `templates/portal-administrativo/rrhh/trabajadores/form.html` | Create | Create/edit modal fragment |
| `templates/portal-administrativo/rrhh/trabajadores/detail.html` | Create | Detail with contratos/periodos sub-tabs |
| `templates/portal-administrativo/rrhh/trabajadores/row.html` | Create | Table row fragment for HTMX |
| `templates/portal-administrativo/rrhh/contratos/list.html` | Create | List by trabajador, estado badges |
| `templates/portal-administrativo/rrhh/contratos/form.html` | Create | Create/edit modal fragment |
| `templates/portal-administrativo/rrhh/contratos/detail.html` | Create | Detail view |
| `templates/portal-administrativo/rrhh/contratos/row.html` | Create | Table row with action buttons |
| `templates/portal-administrativo/rrhh/contratos/action-confirm.html` | Create | Resolver/suspender/reactivar confirmation modal |
| `sidebar.html` | Modify | Add RRHH sub-items: Trabajadores, Contratos |
| `SecurityConfig.java` | None | No new security config needed |

## Template Structure

```
templates/portal-administrativo/rrhh/
├── trabajadores/
│   ├── list.html          ← full page, extends portal.html layout
│   ├── table.html         ← HTMX fragment (tbody for filter refresh)
│   ├── form.html          ← HTMX modal fragment (create/edit)
│   ├── detail.html        ← full page, sub-tabs for contratos/periodos
│   └── row.html           ← <tr> fragment for HTMX replace
└── contratos/
    ├── list.html           ← extends portal.html
    ├── table.html          ← HTMX fragment
    ├── form.html           ← HTMX modal
    ├── detail.html         ← full page
    ├── row.html            ← <tr> with estado badge + action buttons
    └── action-confirm.html ← confirmation modal
```

## HTMX Interaction Patterns

| Pattern | Trigger | Target | Event |
|---------|---------|--------|-------|
| Filter list | Select change or keyup on search | `#table-container` | `hx-get` with params |
| Open create modal | Click "Nuevo" button | `#modal-container` | `hx-get` loads form fragment |
| Submit form | Click "Guardar" in modal | `#modal-container` / `#table-body` | `hx-post` → close modal + refresh table |
| Open detail | Click row or "Ver" button | Full page navigation | Normal `GET` |
| Load sub-tab | Click tab | `#sub-tab-content` | `hx-get` |
| Confirm action | Click "Resolver" button | `#modal-container` | `hx-get` loads confirmation; `hx-post` submits |

## Controller Method Mapping

```
TrabajadorPortalController:
  GET    /administrativo/rrhh/trabajadores         → list()
  GET    /administrativo/rrhh/trabajadores/table    → tableFragment() (HTMX)
  GET    /administrativo/rrhh/trabajadores/nuevo    → createForm() (HTMX modal)
  POST   /administrativo/rrhh/trabajadores          → create()
  GET    /administrativo/rrhh/trabajadores/{id}     → detail()
  GET    /administrativo/rrhh/trabajadores/{id}/editar → editForm() (HTMX modal)
  POST   /administrativo/rrhh/trabajadores/{id}     → update()
  POST   /administrativo/rrhh/trabajadores/{id}/reingreso → reingreso()

ContratoPortalController:
  GET    /administrativo/rrhh/contratos              → list()
  GET    /administrativo/rrhh/contratos/nuevo        → createForm() (HTMX modal)
  POST   /administrativo/rrhh/contratos              → create()
  GET    /administrativo/rrhh/contratos/{id}         → detail()
  GET    /administrativo/rrhh/contratos/{id}/editar  → editForm() (HTMX modal)
  POST   /administrativo/rrhh/contratos/{id}         → update()
  POST   /administrativo/rrhh/contratos/{id}/resolver   → resolver()
  POST   /administrativo/rrhh/contratos/{id}/suspender  → suspender()
  POST   /administrativo/rrhh/contratos/{id}/reactivar  → reactivar()
```

## Sidebar Changes

Add sub-items under RRHH following the existing Seguridad sub-items pattern:

```html
<a th:href="@{/administrativo/rrhh/trabajadores}" ...>Trabajadores</a>
<a th:href="@{/administrativo/rrhh/contratos}" ...>Contratos</a>
```

Active-page detection: activePage == `trabajadores` or `contratos`.

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Controller | HTTP GET/POST rendering | Spring MVC MockMvc — verify status, view name, model attributes |
| HTMX | Fragment responses | MockMvc with `hx-request` header, verify response body contains expected fragments |
| Security | Authority enforcement | `@WithMockUser` variations — 200 vs 403 |

No service-layer tests needed (backend already covered).

## Migration / Rollout

No migration required. Rollback: delete controllers, templates, revert sidebar.

## Open Questions

- [ ] Persona search/select for Trabajador create: reuse existing PersonaRepository or build specific?
