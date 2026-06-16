# Spec: Portal Caja

## Change
frontend-portales / Phase 2

## Requirements

### CAJ-001: Caja Portal Entry
**Phase**: 2
**Description**: `GET /caja` served by `CajaPortalController` showing dashboard.
- Dashboard: summary cards (sesión actual, última liquidación, comprobantes hoy)
- Uses `portal-caja/layouts/portal.html` layout
- Access: `@PreAuthorize("hasAnyAuthority('caja:ver', 'ROLE_ADMIN')")`

### CAJ-002: Caja Theme
**Phase**: 2
**Description**: Portal uses teal theme via CSS variables.
- `--portal-primary: #14b8a6`
- `--portal-secondary: #0d9488`

### CAJ-003: Caja Navigation
**Phase**: 2
**Description**: Navigation sidebar with links:
- Dashboard, Liquidaciones, Comprobantes, Sesión Caja, Empresas, Tarifario
- Fine-grained items use `sec:authorize` (e.g., only show "Emitir Comprobante" if user has `caja:crear`)

### CAJ-004: Template Migration
**Phase**: 2
**Description**: Existing Caja templates under `templates/caja/` migrate from `layout:decorate="~{layouts/main}"` to `layout:decorate="~{portal-caja/layouts/portal}"`.

### CAJ-005: Access Control
**Phase**: 2
**Description**: All Caja portal endpoints require `caja:ver` or `ROLE_ADMIN`.
- Individual actions within the portal use existing `caja:crear`, `caja:editar` permissions

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| CAJ-001-1 | Caja dashboard loads | User has caja:ver | GET /caja | Dashboard with summary cards rendered |
| CAJ-001-2 | Caja access denied | User without caja:ver | GET /caja | 403 Forbidden |
| CAJ-004-1 | Existing template uses caja layout | Template uses old main layout | Migrate | Template renders with caja portal chrome |

