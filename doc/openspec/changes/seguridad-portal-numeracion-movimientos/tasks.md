# Tasks: Portal Seguridad y Control Centralizado de Numeración

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~1600 |
| 450-line budget risk | High |
| Chained PRs recommended | Yes |
| Split | PR 1a → 1b → 1c → 1d → PR 2 → PR 3 |
| Delivery strategy | ask-always |
| Chain strategy | feature-branch-chain |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
450-line budget risk: High

### Suggested Work Units

| Unit | Goal | Base | ~Líneas |
|------|------|------|---------|
| 1a | Foundation: entities, repos, services, migration V43, DataInitializer seed | Tracker | ~415 |
| 1b | Portal Base: controller, layout, sidebar, header, dashboard, success handler | PR 1a branch | ~410 |
| 1c | User & Role CRUD: usuarios, roles, permisos templates | PR 1b branch | ~350 |
| 1d | Config + Numeración + Tipos Movimiento templates | PR 1c branch | ~310 |
| 2 | Migrar ComprobanteService a NumeracionControlService | PR 1d branch | ~50 |
| 3 | Migrar VentaService + auto-HC en PacienteService | PR 2 branch | ~100 |

Tracker: `feature/seguridad-portal-numeracion` (base: main).

## Fase 1a: Foundation — Entities + Services + Migration (PR 1a)

- [x] 1a.1 Crear `NumeracionControl` entity con unique constraint `(entidad, serie, anio)`
- [x] 1a.2 Crear `NumeracionControlRepository`
- [x] 1a.3 Crear `NumeracionControlService` con `nextCorrelativo()` sincronizado + `FOR UPDATE`
- [x] 1a.4 Crear `TipoMovimiento` entity con `codigo` unique
- [x] 1a.5 Crear `TipoMovimientoRepository`
- [x] 1a.6 Crear `TipoMovimientoService` CRUD
- [x] 1a.7 Crear DTOs: `NumeracionControlResponse`, `TipoMovimientoResponse`
- [x] 1a.8 Crear migración V43: `tb_numeracion_control`, `tb_tipos_movimiento`, seed data, fix V32 CHECK
- [x] 1a.9 Modificar `DataInitializer`: seed 5 permisos nuevos, 5 tipos movimiento, 3 numeración entries
- [x] 1a.10 Test: compile + Flyway migration verify

## Fase 1b: Portal Base — Controller + Layout + Dashboard (PR 1b)

- [x] 1b.1 Crear `SeguridadPortalController` con endpoints dashboard, usuarios, roles, permisos, config-api, numeración, tipos-movimiento
- [x] 1b.2 Crear layout `portal.html` con CSS variables indigo
- [x] 1b.3 Crear fragments: `header.html`, `sidebar.html`
- [x] 1b.4 Crear template `dashboard.html`
- [x] 1b.5 Modificar `PortalAuthenticationSuccessHandler`: redirect a `/seguridad` para `seguridad:ver`
- [x] 1b.6 Test: compile + smoke test portal carga

## Fase 1c: User & Role CRUD Templates (PR 1c)

- [ ] 1c.1 Crear templates: `usuarios.html`, `usuario-form.html`
- [ ] 1c.2 Crear templates: `roles.html`, `rol-form.html`
- [ ] 1c.3 Crear template: `permisos.html`
- [ ] 1c.4 Test: compile + navegación CRUD

## Fase 1d: Numeración + Tipos Movimiento + Config Templates (PR 1d)

- [ ] 1d.1 Crear template: `config-api.html`
- [ ] 1d.2 Crear templates: `numeracion.html`, `numeracion-form.html`
- [ ] 1d.3 Crear templates: `tipos-movimiento.html`, `tipo-movimiento-form.html`
- [ ] 1d.4 Crear template: `cambiar-contrasena.html` (o actualizar existente)
- [ ] 1d.5 Test: compile + navegación CRUD

## Fase 2: Migración Comprobante (PR 2)

- [ ] 2.1 Modificar `ComprobanteService`: inyectar `NumeracionControlService`
- [ ] 2.2 Reemplazar `nextCorrelativo()` inline por llamada a servicio centralizado

## Fase 3: Migración Venta + HC (PR 3)

- [ ] 3.1 Modificar `PacienteService` con auto-generación HC vía `NumeracionControlService`
- [ ] 3.2 Modificar `PacienteController`: inyectar `NumeracionControlService` para auto-HC

## Fase 4: Testing (transversal, por PR)

- [ ] 4.1 Unit: `NumeracionControlService.nextCorrelativo()` — secuencia, prefijo, padding, año, concurrencia
- [ ] 4.2 Unit: `TipoMovimientoService` — CRUD, duplicado, toggle activo, filtro módulo
- [ ] 4.3 Unit: `SeguridadPortalController` — vistas y atributos `activePage`
- [ ] 4.4 Integration: V43 Flyway — tablas y seed data existen
- [ ] 4.5 Integration: `NumeracionControlService` real — `FOR UPDATE` con dos hilos concurrentes
- [ ] 4.6 Integration: V32 CHECK fix — insertar DEVOLUCION post-migración
