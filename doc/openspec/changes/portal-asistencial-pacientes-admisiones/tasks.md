# Tasks: Portal Asistencial — Pacientes y Admisiones

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~450–550 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1: Pacientes (controllers + templates + sidebar). PR 2: Admisiones wizard |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

## Phase 1: View Controllers

- [x] 1.1 Create `PacientePortalController.java` — GET /asistencial/pacientes (search), GET /asistencial/pacientes/{id} (detail)
- [ ] 1.2 Create `AdmisionPortalController.java` — GET /asistencial/admisiones (list), GET /asistencial/admisiones/nueva (wizard), POST wizard endpoints
- [ ] 1.3 Create `DiagnosticoForm` DTO for wizard diagnosis step

## Phase 2: Pacientes Templates

- [x] 2.1 Create `portal-asistencial/pacientes/search.html` — search form + HTMX results container
- [x] 2.2 Create `portal-asistencial/pacientes/detail.html` — demographics + admissions list
- [x] 2.3 Create `portal-asistencial/pacientes/fragments/search-form.html` — HTMX async search
- [ ] 2.4 Create `portal-asistencial/pacientes/fragments/patient-row.html` — single result row

## Phase 3: Admisiones Templates

- [ ] 3.1 Create `portal-asistencial/admisiones/list.html` — pending list with 2h warning banner
- [ ] 3.2 Create `portal-asistencial/admisiones/nueva.html` — wizard container + progress indicator
- [ ] 3.3 Create `portal-asistencial/admisiones/wizard/step-paciente.html` — search/select patient
- [ ] 3.4 Create `portal-asistencial/admisiones/wizard/step-paquete.html` — package selection (name + 1 día badge)
- [ ] 3.5 Create `portal-asistencial/admisiones/wizard/step-cama.html` — bed assign/skip
- [ ] 3.6 Create `portal-asistencial/admisiones/wizard/step-diagnostico.html` — CIE-11 autocomplete
- [ ] 3.7 Create `portal-asistencial/admisiones/wizard/step-completado.html` — completion confirmation

## Phase 4: Sidebar / Dashboard Updates

- [x] 4.1 Update `portal-asistencial/fragments/sidebar.html` — replace # links with `/asistencial/pacientes`, `/asistencial/admisiones`
- [ ] 4.2 Update `portal-asistencial/dashboard.html` — quick-access card hrefs to real routes

## Phase 5: Testing

- [x] 5.1 @WebMvcTest for PacientePortalController — route resolution, model attrs, @PreAuthorize
- [ ] 5.2 @WebMvcTest for AdmisionPortalController — wizard flow, session state, permissions
- [ ] 5.3 Unit test for 2-hour alert condition logic

## Implementation Order

Controllers first (Phase 1), then Pacientes templates (Phase 2), then Admisiones templates (Phase 3), then sidebar/dashboard link updates (Phase 4), finally tests (Phase 5). This order allows each template to be tested immediately after creation.
