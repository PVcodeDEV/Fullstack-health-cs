# Design: Portal Asistencial — Pacientes y Admisiones

## Technical Approach

Create two Spring MVC view controllers (`PacientePortalController`, `AdmisionPortalController`) that extend the existing portal pattern established by `AsistencialPortalController`. Each controller renders Thymeleaf templates under `portal-asistencial/pacientes/` and `portal-asistencial/admisiones/`, delegating data fetching to existing service classes. The Admisiones wizard uses server-side session state across HTMX-driven partial steps. Sidebar and dashboard links are updated from `#` to real routes.

## Architecture Decisions

### Decision: Wizard state management

| Option | Tradeoff |
|--------|----------|
| **Server-side session** (chosen) | State survives page reload; no JS framework needed; simple HttpSession attributes |
| Client-side JS object | Lost on reload; requires JS framework for persistence |
| URL query params | Exposes all state in URL; complex encoding for multi-step data |

**Choice**: Server-side session attributes via HttpSession, cleared on wizard completion or cancel.

### Decision: Route prefix

All portal routes use `/asistencial/` prefix matching the existing `AsistencialPortalController` pattern. This keeps URL space consistent for sidebar active-page highlighting.

### Decision: Permission model

Portal controllers use `@PreAuthorize` with the same authority strings as REST endpoints. `ROLE_ADMIN` bypasses all checks.

## Data Flow

### Pacientes search flow

```
[Search Form]
    │ GET /asistencial/pacientes?q=...
    ▼
PacientePortalController.search(q)
    │
    ▼
PacienteService.searchByDocumento() / searchByNombres()
    │
    ▼
[Results table — Thymeleaf fragment via HTMX]
```

### Admisiones wizard flow

```
GET /asistencial/admisiones/nueva
    │
    ▼
[Step 1: Buscar Paciente] ──HTMX hx-post──→ wizard/paciente (session: pacienteId)
    │
    ▼
[Step 2: Seleccionar Paquete] ──HTMX──→ wizard/paquete (session: paqueteId)
    │
    ▼
[Step 3: Asignar/Saltar Cama] ──HTMX──→ wizard/cama (session: camaId or null)
    │
    ▼
[Step 4: Registrar Diagnóstico] ──HTMX──→ wizard/diagnostico (POST crea Cuenta + HC)
    │
    ▼
[Completado: Resumen de Cuenta creada]
```

### 2-hour alert flow

```
AdmisionPortalController.list()
    │
    ▼
For each SolicitudHospitalizacion with status PENDIENTE:
    if (fechaCreacion + 2h < now) → add to warning list
    │
    ▼
[Warning banner in admissions list view + dashboard data]
```

## File Changes

| File | Action | Description |
|------|--------|-------------|
| `com/clinica/clinica/controller/PacientePortalController.java` | Create | View controller for pacientes search/detail |
| `com/clinica/clinica/controller/AdmisionPortalController.java` | Create | View controller for admisiones wizard + list |
| `templates/portal-asistencial/pacientes/search.html` | Create | Search page with HTMX async results |
| `templates/portal-asistencial/pacientes/detail.html` | Create | Patient detail with demographics + admissions |
| `templates/portal-asistencial/pacientes/fragments/` | Create | HTMX fragments: search-form, patient-row |
| `templates/portal-asistencial/admisiones/nueva.html` | Create | Wizard container with progress indicator |
| `templates/portal-asistencial/admisiones/list.html` | Create | Pending admissions list with 2h alerts |
| `templates/portal-asistencial/admisiones/wizard/` | Create | Step templates: paciente, paquete, cama, diagnostico, completado |
| `templates/portal-asistencial/fragments/sidebar.html` | Modify | Replace # links with real routes |
| `templates/portal-asistencial/dashboard.html` | Modify | Update quick-access card hrefs |

## Interfaces / Contracts

### PacientePortalController

| Method | Route | @PreAuthorize | Returns |
|--------|-------|---------------|---------|
| `searchPacientes(q)` | GET /asistencial/pacientes | `paciente:ver` | search.html |
| `detallePaciente(id)` | GET /asistencial/pacientes/{id} | `paciente:ver` | detail.html |

### AdmisionPortalController

| Method | Route | @PreAuthorize | Returns |
|--------|-------|---------------|---------|
| `listAdmisiones()` | GET /asistencial/admisiones | `admision:ver` | list.html |
| `nuevaAdmision()` | GET /asistencial/admisiones/nueva | `admision:crear` | nueva.html |
| `wizardPaciente(pacienteId)` | POST /asistencial/admisiones/wizard/paciente | `admision:crear` | step-paquete.html |
| `wizardPaquete(paqueteId)` | POST /asistencial/admisiones/wizard/paquete | `admision:crear` | step-cama.html |
| `wizardCama(camaId?)` | POST /asistencial/admisiones/wizard/cama | `admision:asignar_cama` | step-diagnostico.html |
| `wizardDiagnostico(form)` | POST /asistencial/admisiones/wizard/diagnostico | `admision:editar` | step-completado.html |

## Testing Strategy

| Layer | What to Test | Approach |
|-------|-------------|----------|
| View Controllers | Route resolution, model attributes, @PreAuthorize | @WebMvcTest with security mock users |
| HTMX fragments | Partial rendering, correct fragment selection | Template rendering assertions |
| Wizard flow | Session state transitions, completion | MockHttpSession in controller tests |
| 2-hour alert | Warning banner condition logic | Unit test on time comparison |

## Migration / Rollout

No migration required. All new files — no existing behavior changes beyond updating sidebar and dashboard links from `#` to real routes.

## Open Questions

None.
