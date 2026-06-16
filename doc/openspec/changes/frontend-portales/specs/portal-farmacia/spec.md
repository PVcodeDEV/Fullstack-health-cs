# Spec: Portal Farmacia

## Change
frontend-portales / Phase 4

## Requirements

### FARM-001: Farmacia Portal Entry
**Phase**: 4
**Description**: `GET /farmacia` served by `FarmaciaPortalController` showing dashboard.
- Dashboard: stock alerts, pending dispatches, recent transactions
- Uses `portal-farmacia/layouts/portal.html` layout
- Access: `@PreAuthorize("hasAnyAuthority('farmacia:ver', 'ROLE_ADMIN')")`

### FARM-002: Farmacia Theme
**Phase**: 4
**Description**: Portal uses green theme via CSS variables.
- `--portal-primary: #22c55e`
- `--portal-secondary: #16a34a`

### FARM-003: Farmacia Navigation
**Phase**: 4
**Description**: Navigation sidebar with links:
- Dashboard, Despacho, Stock, Mermas

### FARM-004: Access Control
**Phase**: 4
**Description**: All portal endpoints require `farmacia:ver` or `ROLE_ADMIN`.

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| FARM-001-1 | Farmacia dashboard loads | User has farmacia:ver | GET /farmacia | Dashboard with stock alerts rendered |
| FARM-001-2 | Farmacia access denied | User without farmacia:ver | GET /farmacia | 403 Forbidden |

