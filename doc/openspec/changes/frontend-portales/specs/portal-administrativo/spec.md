# Spec: Portal Administrativo

## Change
frontend-portales / Phase 5

## Requirements

### ADMP-001: Administrativo Portal Entry
**Phase**: 5
**Description**: `GET /administrativo` served by `AdministrativoPortalController` showing dashboard.
- Dashboard: links to RRHH, Maestros, Usuarios, Seguridad
- Uses `portal-administrativo/layouts/portal.html` layout
- Access: `@PreAuthorize("hasAnyAuthority('administrativo:ver', 'ROLE_ADMIN')")`

### ADMP-002: Administrativo Theme
**Phase**: 5
**Description**: Portal uses slate theme via CSS variables.
- `--portal-primary: #64748b`
- `--portal-secondary: #475569`

### ADMP-003: Administrativo Navigation
**Phase**: 5
**Description**: Navigation sidebar with links:
- Dashboard, RRHH, Maestros, Usuarios, Seguridad

### ADMP-004: Access Control
**Phase**: 5
**Description**: All portal endpoints require `administrativo:ver` or `ROLE_ADMIN`.
- ADMIN user sees portal switcher to navigate to other portals

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| ADMP-001-1 | Admin dashboard loads | User has administrativo:ver | GET /administrativo | Dashboard with links rendered |
| ADMP-001-2 | Admin access denied | User without administrativo:ver | GET /administrativo | 403 Forbidden |
| ADMP-001-3 | ADMIN sees portal switcher | User has ROLE_ADMIN | GET /administrativo | Portal switcher visible in layout |

