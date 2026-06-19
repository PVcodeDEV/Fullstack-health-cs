# Tasks: RRHH Portal Trabajador & Contrato

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~650-750 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR #1: Trabajador, PR #2: Contrato |
| Delivery strategy | ask-on-risk |
| Chain strategy | feature-branch-chain |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | PR | Base |
|------|------|----|------|
| 1 | Trabajador portal controller + templates | PR #1 | `feature/rrhh-portal` |
| 2 | Contrato portal controller + templates | PR #2 | PR #1 branch |

## Phase 1: Trabajador Portal (PR #1)

- [x] 1.1 Create `TrabajadorPortalController.java` in `com.clinica.rrhh.controller` — list, create, edit, detail, reingreso
- [x] 1.2 Create `list.html` — trabajador list page with layout, search/filter form, table container
- [x] 1.3 Create `table.html` — HTMX fragment for filtered table body
- [x] 1.4 Create `row.html` — HTMX table row fragment with edit/detail actions
- [x] 1.5 Create `form.html` — HTMX modal fragment for create/edit with TrabajadorRequest fields + validation
- [x] 1.6 Create `detail.html` — trabajador detail with contratos/periodos HTMX sub-tabs
- [x] 1.7 Modify `sidebar.html` — add Trabajadores and Contratos sub-links under RRHH
- [x] 1.8 Write controller tests — MockMvc for list, create, edit flows + authority checks

## Phase 2: Contrato Portal (PR #2)

- [ ] 2.1 Create `ContratoPortalController.java` in `com.clinica.rrhh.controller` — list, create, edit, detail, resolver, suspender, reactivar
- [ ] 2.2 Create `list.html` — contrato list page with layout, trabajador selector, table
- [ ] 2.3 Create `table.html` — HTMX fragment for contrato table body with estado badges
- [ ] 2.4 Create `row.html` — table row with estado badge + action buttons (resolver/suspender/reactivar)
- [ ] 2.5 Create `form.html` — HTMX modal for create/edit with ContratoRequest fields
- [ ] 2.6 Create `action-confirm.html` — HTMX confirmation modal for resolver/suspender/reactivar
- [ ] 2.7 Create `detail.html` — contrato detail with all fields + linked trabajador info
- [ ] 2.8 Write controller tests — MockMvc for list, create, resolver, suspender flows + authority checks
