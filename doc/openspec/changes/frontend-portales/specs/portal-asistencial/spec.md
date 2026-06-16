# Spec: Portal Asistencial

## Change
frontend-portales / Phase 3

## Requirements

### ASIS-001: Asistencial Portal Entry
**Phase**: 3
**Description**: `GET /asistencial` served by `AsistencialPortalController` showing dashboard.
- Dashboard: quick access to pacientes, admisiones, búsqueda rápida
- Uses `portal-asistencial/layouts/portal.html` layout
- Access: `@PreAuthorize("hasAnyAuthority('asistencial:ver', 'ROLE_ADMIN')")`

### ASIS-002: Asistencial Theme
**Phase**: 3
**Description**: Portal uses blue theme via CSS variables.
- `--portal-primary: #3b82f6`
- `--portal-secondary: #2563eb`

### ASIS-003: Asistencial Navigation
**Phase**: 3
**Description**: Navigation sidebar with links:
- Dashboard, Pacientes, Admisiones, Historia Clínica, SOP

### ASIS-004: Access Control
**Phase**: 3
**Description**: All portal endpoints require `asistencial:ver` or `ROLE_ADMIN`.

## Scenarios

| ID | Scenario | Given | When | Then |
|----|----------|-------|------|------|
| ASIS-001-1 | Asistencial dashboard loads | User has asistencial:ver | GET /asistencial | Dashboard with quick access cards rendered |
| ASIS-001-2 | Asistencial access denied | User without asistencial:ver | GET /asistencial | 403 Forbidden |

