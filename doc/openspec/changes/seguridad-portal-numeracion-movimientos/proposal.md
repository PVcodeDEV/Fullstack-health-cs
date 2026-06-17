# Propuesta: Portal Seguridad y Control Centralizado de Numeración y Tipos de Movimiento

## Propósito

Construir la interfaz de administración del módulo seguridad (portal Thymeleaf) y centralizar allí el control de numeración de comprobantes, historias clínicas y tipos de movimiento, por ser funcionalidades críticas que requieren gobierno centralizado.

## Alcance

### Incluye
- Portal Seguridad standalone: CRUD usuarios, roles, permisos, config API
- `NumeracionControl` entity + servicio con `SELECT ... FOR UPDATE` para secuencias sin saltos
- Gestión de tipos de movimiento como datos maestros (`tb_tipos_movimiento`)
- Migración incremental: comprobante (caja), venta (farmacia), HC (paciente)
- 5 nuevos permisos seed + corrección CHECK constraint V32 (falta `DEVOLUCION`)

### Excluye
- Restablecimiento de contraseña, OAuth2/LDAP, clustering de sesiones, notificaciones email, auditoría de numeración

## Capacidades

### Nuevas
- `portal-seguridad`: UI Thymeleaf para administración de seguridad
- `numeracion-control`: Servicio centralizado de numeración correlativa sin saltos
- `tipo-movimiento`: Mantenimiento de tipos de movimiento como datos maestros

### Modificadas
- `modulo-autorizacion`: Nuevos permisos seed y asignación a roles
- `modulo-autenticación`: Template de cambio de contraseña desde portal

## Enfoque

1. **Portal**: Mismo patrón que portales existentes — `SeguridadPortalController`, layout indigo, sidebar con enlaces
2. **Numeración**: Entity con `entidad`, `serie`, `correlativo_actual`. Servicio con `synchronized` + `SELECT ... FOR UPDATE` en transacción. Migrar módulos incrementalmente
3. **Tipos movimiento**: Tabla con seed data + CRUD desde portal + fix CHECK constraint
4. **Permisos**: DataInitializer actualizado con 5 nuevos permisos

## Áreas Afectadas

| Área | Impacto |
|------|---------|
| `seguridad/*/` | Nuevos controller, entity, service, repository |
| `seguridad/config/DataInitializer` | Modificado — nuevos permisos seed |
| `caja/comprobante/` | Modificado — migrar a numeración centralizada |
| `farmacia/venta/` | Modificado — migrar a numeración centralizada |
| `clinica/paciente/` | Modificado — auto-generar HC |
| `db/migration/` | Nueva V42+ |
| `frontend/` | Nuevos templates Thymeleaf |

## Riesgos

| Riesgo | Prob. | Mitigación |
|--------|-------|------------|
| Concurrencia en `FOR UPDATE` | Media | Transacciones cortas, índice `(entidad, serie)` |
| Migración comprobantes existentes | Baja | Columna serie con default "001", backward compatible |

## Rollback

Eliminar tablas nuevas, revertir migración V42+, restaurar DataInitializer original. Módulos vuelven a numeración inline sin pérdida de datos.

## Criterios de Éxito

- [ ] Portal Seguridad en `/seguridad` con sidebar y theme indigo
- [ ] CRUD usuarios, roles, permisos funcional desde UI
- [ ] Nuevo comprobante recibe número del servicio centralizado
- [ ] Nuevo paciente recibe HC autogenerado
- [ ] Tipos de movimiento se gestionan desde el portal
- [ ] CHECK constraint V32 corregido
