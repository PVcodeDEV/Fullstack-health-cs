# Diseño: Portal Seguridad y Control Centralizado de Numeración

## Enfoque Técnico

Construir un portal Thymeleaf independiente en `/seguridad` que centraliza la administración de usuarios, roles, permisos, numeración correlativa y tipos de movimiento. El diseño sigue el mismo patrón de los portales existentes (asistencial, administrativo, caja, farmacia): controlador con `@PreAuthorize`, layout con variables CSS para tema indigo, sidebar con `activePage` para resaltar sección activa, y fragmentos Thymeleaf reutilizables.

La numeración correlativa se implementa como servicio propio en el módulo `seguridad` con bloqueo pesimista (`SELECT ... FOR UPDATE`) para garantizar secuencias sin saltos. Los tipos de movimiento migran de CHECK constraint hardcodeado a entidad propia manejable desde el portal.

## Decisiones Arquitectónicas

### Decisión: Patrón de Portal Seguridad
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| `SeguridadPortalController` separado | Sigue el patrón exacto de `AsistencialPortalController` | **Elegido** — consistencia con portales existentes |
| Fusionar con `SeguridadAdminController` | Mezcla REST API con vistas Thymeleaf | Rechazado — viola separación de concerns |
| **Rationale**: Todos los portales existentes usan un controlador dedicado con `@PreAuthorize` + Thymeleaf. Mantener la misma convención evita sorpresas y facilita el mantenimiento futuro. El controller se mapea a `/seguridad/**` con `@PreAuthorize("hasAnyAuthority('seguridad:ver', 'ROLE_ADMIN')")`. |

### Decisión: Estrategia de Lock para Numeración
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| `SELECT ... FOR UPDATE` pesimista | Garantiza sin saltos; impacto mínimo en ventana crítica baja (~1/s) | **Elegido** — requisito del spec R-002 |
| `@Version` optimista | Permite saltos en alta concurrencia | Rechazado — no cumple el requisito de secuencias sin huecos |
| Secuencia nativa DB (SERIAL) | Depende de dialecto; difícil migrar entre entidades | Rechazado — la entidad requiere control por año y serie |
| **Rationale**: El spec exige garantía de NO saltos. Para un ERP clínico con volumen bajo de transacciones (~40 empleados), `FOR UPDATE` es suficiente. El servicio expone `nextCorrelativo()` como método `synchronized` + `@Transactional` con índice único en `(entidad, serie, año)`. |

### Decisión: Migración Incremental en 3 PRs
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Migrar todo en un PR | Riesgo alto; toca caja, farmacia y clínica simultáneamente | Rechazado — demasiado riesgo |
| **3 PRs secuenciales** | Aísla cambios; cada PR desplegable individualmente | **Elegido** — menor riesgo, revisable |
| **Rationale**: PR1 crea entidades + portal + seed data. PR2 migra `ComprobanteService` a usar `NumeracionControlService`. PR3 migra farmacia (ventas) y clínica (auto-HC). Cada PR es autónomo y desplegable. El servicio centralizado convive con el código inline hasta que se migra cada consumidor. |

### Decisión: Naming de Tablas y Columnas
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| `tb_numeracion_control` con prefijo `numc_` | Consistente con V12 (`usu_`, `rol_`, `per_`) | **Elegido** — sigue la convención del módulo seguridad |
| `tb_numeracion` + prefijo `num_` | Menos verbose pero rompe consistencia | Rechazado — todos los prefijos son 4 caracteres |
| **Rationale**: La migración V12 usa prefijos de 4 caracteres (`usu_`, `rol_`, `per_`). Para `tb_numeracion_control` usamos `numc_` y para `tb_tipos_movimiento` usamos `tim_`. Ambos siguen la convención establecida. |

### Decisión: Tema Indigo para Portal Seguridad
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Variables CSS indigo/púrpura | Distingue visualmente el portal de administración | **Elegido** — diferenciación clara en la UI |
| Gris como administrativo | Confunde con el portal administrativo existente | Rechazado — el administrativo ya usa slate/gris |
| **Rationale**: El portal asistencial es azul, administrativo es slate, caja es teal, farmacia es verde. El portal seguridad usa indigo/púrpura para diferenciarse claramente como herramienta de gobierno del sistema. |

### Decisión: Corrección V32 CHECK vía Migración
| Opción | Tradeoff | Decisión |
|--------|----------|----------|
| Eliminar CHECK y migrar a FK contra `tb_tipos_movimiento` | Correcto arquitectónicamente pero requiere refactor mayor en farmacia | **Elegido para futuro** — se documenta pero no se ejecuta ahora |
| Agregar DEVOLUCION al CHECK existente | Mínimo cambio, compatible hacia atrás | **Elegido para ahora** — bajo riesgo, desbloquea DEVOLUCION |
| **Rationale**: Migrar a FK requiere cambiar el modelo de farmacia y coordinar con V32. Para desbloquear el seed de DEVOLUCION en este cambio, simplemente alteramos el CHECK constraint para incluir DEVOLUCION. La migración a FK queda como mejora futura documentada. |

## Flujo de Datos

### Numeración Centralizada

```
ComprobanteService.emitir()
  ──→ NumeracionControlService.nextCorrelativo("COMPROBANTE", "001")
        ──→ @Transactional
              ──→ SELECT numc_* FROM tb_numeracion_control
                  WHERE numc_entidad=? AND numc_serie=? AND numc_anio=?
                  FOR UPDATE
              ──→ correlativo = ++correlativo_actual
              ──→ UPDATE tb_numeracion_control SET numc_correlativo_actual=?
              ──→ return prefijo + pad(correlativo)
```

### Portal Seguridad

```
GET /seguridad/usuarios
  ──→ SeguridadPortalController
        ──→ @PreAuthorize("hasAnyAuthority('seguridad:ver','ROLE_ADMIN')")
        ──→ model.addAttribute("activePage", "usuarios")
        ──→ model.addAttribute("portalHeader", "portal-seguridad/fragments/header")
        ──→ model.addAttribute("portalSidebar", "portal-seguridad/fragments/sidebar")
        ──→ return "portal-seguridad/usuarios"
              ──→ layout:decorate="~{portal-seguridad/layouts/portal}"
                    ──→ layout:decorate="~{layouts/base}"
```

### Inicio de Sesión con Password Change

```
POST /login (credenciales correctas)
  ──→ PortalAuthenticationSuccessHandler
        ──→ passwordChangeRequired=true?
              ──→ redirect /cambiar-contrasena
        ──→ ROLE_ADMIN?
              ──→ redirect /administrativo
        ──→ seguridad:ver?
              ──→ redirect /seguridad
```

## Cambios en Archivos

| Archivo | Acción | Descripción |
|---------|--------|-------------|
| **PR 1 — Portal + Entidades + Seed** | | |
| `backend/src/main/java/com/clinica/seguridad/entity/NumeracionControl.java` | Crear | Entity `tb_numeracion_control` con `(entidad, serie, año)` unique |
| `backend/src/main/java/com/clinica/seguridad/entity/TipoMovimiento.java` | Crear | Entity `tb_tipos_movimiento` con `codigo` unique |
| `backend/src/main/java/com/clinica/seguridad/repository/NumeracionControlRepository.java` | Crear | Repository con `findByEntidadAndSerieAndAnio()` |
| `backend/src/main/java/com/clinica/seguridad/repository/TipoMovimientoRepository.java` | Crear | Repository con `findByCodigo()`, `findByModulo()` |
| `backend/src/main/java/com/clinica/seguridad/service/NumeracionControlService.java` | Crear | Servicio con `nextCorrelativo()` sincronizado + `FOR UPDATE` |
| `backend/src/main/java/com/clinica/seguridad/service/TipoMovimientoService.java` | Crear | CRUD para tipos de movimiento |
| `backend/src/main/java/com/clinica/seguridad/controller/SeguridadPortalController.java` | Crear | Controller Thymeleaf en `/seguridad` con dashboard, usuarios, roles, permisos, API config, numeración, tipos movimiento |
| `backend/src/main/java/com/clinica/seguridad/dto/NumeracionControlResponse.java` | Crear | DTO para respuesta de numeración |
| `backend/src/main/java/com/clinica/seguridad/dto/TipoMovimientoResponse.java` | Crear | DTO para respuesta de tipo movimiento |
| `backend/src/main/java/com/clinica/seguridad/dto/CambioContrasenaRequest.java` | Crear | DTO para cambio de contraseña |
| `backend/src/main/resources/db/migration/V43__seguridad_numeracion_tipos_movimiento.sql` | Crear | Migración: `tb_numeracion_control`, `tb_tipos_movimiento`, seed data, fix V32 CHECK |
| `backend/src/main/java/com/clinica/seguridad/bootstrap/DataInitializer.java` | Modificar | Seed 5 permisos nuevos, seed 5 tipos movimiento, seed 3 numeración entries |
| `backend/src/main/resources/templates/portal-seguridad/layouts/portal.html` | Crear | Layout con CSS variables indigo |
| `backend/src/main/resources/templates/portal-seguridad/fragments/header.html` | Crear | Header del portal seguridad |
| `backend/src/main/resources/templates/portal-seguridad/fragments/sidebar.html` | Crear | Sidebar con enlaces: Dashboard, Usuarios, Roles, Permisos, Config API, Numeración, Tipos Movimiento, Cambiar Contraseña |
| `backend/src/main/resources/templates/portal-seguridad/dashboard.html` | Crear | Dashboard con counts summary |
| `backend/src/main/resources/templates/portal-seguridad/usuarios.html` | Crear | Lista de usuarios |
| `backend/src/main/resources/templates/portal-seguridad/usuario-form.html` | Crear | Formulario crear/editar usuario |
| `backend/src/main/resources/templates/portal-seguridad/roles.html` | Crear | Lista de roles |
| `backend/src/main/resources/templates/portal-seguridad/rol-form.html` | Crear | Formulario crear/editar rol con asignación de permisos |
| `backend/src/main/resources/templates/portal-seguridad/permisos.html` | Crear | Lista de permisos con filtro por módulo |
| `backend/src/main/resources/templates/portal-seguridad/config-api.html` | Crear | Edición de configuración API |
| `backend/src/main/resources/templates/portal-seguridad/numeracion.html` | Crear | Lista de control de numeración |
| `backend/src/main/resources/templates/portal-seguridad/numeracion-form.html` | Crear | Formulario crear/editar entrada de numeración |
| `backend/src/main/resources/templates/portal-seguridad/tipos-movimiento.html` | Crear | Lista de tipos de movimiento |
| `backend/src/main/resources/templates/portal-seguridad/tipo-movimiento-form.html` | Crear | Formulario crear/editar tipo de movimiento |
| `backend/src/main/resources/templates/cambiar-contrasena.html` | Crear | Template de cambio de contraseña post-login |
| `backend/src/main/java/com/clinica/seguridad/handler/PortalAuthenticationSuccessHandler.java` | Modificar | Agregar redirect a `/seguridad` para `seguridad:ver` |
| **PR 2 — Migración Comprobante** | | |
| `backend/src/main/java/com/clinica/caja/comprobante/service/ComprobanteService.java` | Modificar | Reemplazar `nextCorrelativo()` inline por `NumeracionControlService` |
| **PR 3 — Migración Farmacia + HC** | | |
| `backend/src/main/java/com/clinica/clinica/paciente/service/PacienteService.java` | Crear | Servicio que usa `NumeracionControlService.nextCorrelativo("HC", "001")` al crear paciente |
| `backend/src/main/java/com/clinica/clinica/paciente/controller/PacienteController.java` | Modificar | Inyectar `NumeracionControlService` para auto-HC |

## Interfaces / Contratos

### NumeracionControlService

```java
package com.clinica.seguridad.service;

/**
 * Servicio centralizado de numeración correlativa sin saltos.
 * Cada llamada a nextCorrelativo() incrementa y retorna bajo FOR UPDATE.
 */
@Service
@Transactional
public class NumeracionControlService {

    /**
     * Retorna el siguiente correlativo formateado para una entidad/serie.
     * Formato: {prefijo}{correlativo_padded}
     * Ejemplo: "HC-000011" o "000006"
     *
     * @param entidad nombre de la entidad (COMPROBANTE, HC, VENTA, etc.)
     * @param serie   serie numérica (001, 002, etc.)
     * @return String con el correlativo formateado
     * @throws IllegalStateException si la entrada está inactiva o no existe
     */
    public synchronized String nextCorrelativo(String entidad, String serie) { ... }
}
```

### NumeracionControl Entity

```java
@Entity
@Table(name = "tb_numeracion_control",
       uniqueConstraints = @UniqueConstraint(columnNames = {"numc_entidad", "numc_serie", "numc_anio"}))
@AttributeOverride(name = "createdAt", column = @Column(name = "numc_created_at"))
@AttributeOverride(name = "updatedAt", column = @Column(name = "numc_updated_at"))
@AttributeOverride(name = "activo", column = @Column(name = "numc_activo"))
public class NumeracionControl extends BaseEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "numc_id") private Long id;

    @Column(name = "numc_entidad", nullable = false, length = 50)     private String entidad;
    @Column(name = "numc_serie", nullable = false, length = 10)       private String serie;
    @Column(name = "numc_correlativo_actual", nullable = false)       private Long correlativoActual = 0L;
    @Column(name = "numc_prefijo", length = 10)                       private String prefijo;
    @Column(name = "numc_longitud_correlativo", nullable = false)     private int longitudCorrelativo = 6;
    @Column(name = "numc_anio", nullable = false)                     private int anio;
}
```

### SeguridadPortalController (endpoints clave)

```java
@Controller
@RequestMapping("/seguridad")
@PreAuthorize("hasAnyAuthority('seguridad:ver', 'ROLE_ADMIN')")
public class SeguridadPortalController {

    @GetMapping("")                          public String dashboard(Model model) { ... }
    @GetMapping("/usuarios")                 public String listUsuarios(Model model) { ... }
    @GetMapping("/usuarios/nuevo")           public String nuevoUsuario(Model model) { ... }
    @PostMapping("/usuarios")                public String crearUsuario(...) { ... }
    @GetMapping("/roles")                    public String listRoles(Model model) { ... }
    @GetMapping("/roles/nuevo")              public String nuevoRol(Model model) { ... }
    @PostMapping("/roles")                   public String crearRol(...) { ... }
    @GetMapping("/permisos")                 public String listPermisos(Model model) { ... }
    @GetMapping("/config-api")               public String listConfigApi(Model model) { ... }
    @PutMapping("/config-api")               public String updateConfigApi(...) { ... }
    @GetMapping("/numeracion")               public String listNumeracion(Model model) { ... }
    @GetMapping("/numeracion/nuevo")         public String nuevaNumeracion(Model model) { ... }
    @PostMapping("/numeracion")              public String crearNumeracion(...) { ... }
    @GetMapping("/tipos-movimiento")         public String listTiposMovimiento(Model model) { ... }
    // + toggle activo endpoints
}
```

## Estrategia de Testing

| Capa | Qué probar | Enfoque |
|------|-----------|---------|
| Unit | `NumeracionControlService.nextCorrelativo()` | Mock repository, probar secuencia, prefijo, padding, año actual, concurrencia simulada |
| Unit | `TipoMovimientoService` CRUD | Mock repository, probar create con duplicado, toggle activo, filtro por módulo |
| Unit | `SeguridadPortalController` | Mock services, probar que retorna vista correcta y atributos `activePage` |
| Integration | V43 migración Flyway | Probar que `tb_numeracion_control` y `tb_tipos_movimiento` existen con seed data |
| Integration | `NumeracionControlService` real | Probar `FOR UPDATE` en H2 con 2 hilos concurrentes (assert correlativos consecutivos) |
| Integration | V32 CHECK fix | Insertar DEVOLUCION después de migración y verificar que no falla |
| E2E | Portal navegación | Navegar `/seguridad/usuarios` autenticado y verificar sidebar activo (manual) |

## Migración / Rollout

**PR 1 — Portal + Entidades + Seed** (este cambio):
1. Aplicar V43 Flyway: crear `tb_numeracion_control`, `tb_tipos_movimiento`, fix V32 CHECK + `ADD COLUMN movs_tipo_movimiento_id`
2. Seed: 3 entries numeración (COMPROBANTE/001, VENTA/001, HC/001), 5 tipos movimiento FARMACIA
3. Seed: 5 nuevos permisos (`seguridad:ver`, `numeracion:ver`, `numeracion:editar`, `tipo-movimiento:ver`, `tipo-movimiento:editar`)
4. Desplegar portal seguridad en `/seguridad`
5. **Sin impacto en operaciones** — los módulos existentes siguen usando su numeración inline

**PR 2 — Migración Comprobante** (futuro):
1. Inyectar `NumeracionControlService` en `ComprobanteService`
2. Reemplazar `nextCorrelativo()` privado por llamada al servicio centralizado
3. Agregar columna `numc_serie` opcional en Comprobante para tracking
4. Verificar que comprobantes existentes mantienen su correlativo

**PR 3 — Migración Venta + HC** (futuro):
1. Inyectar `NumeracionControlService` en servicios de farmacia (venta)
2. Auto-generar HC al crear paciente usando `NumeracionControlService`

**Rollback**: Eliminar V43, restaurar DataInitializer original. Módulos vuelven a numeración inline. CHECK constraint de V32 vuelve a su estado original.

## Preguntas Abiertas

- [ ] El spec de autenticación menciona que el template `/cambiar-contrasena` debe ser accesible desde sidebar de portal-seguridad. ¿Debe ser una página independiente o un modal HTMX? Por ahora asumimos página independiente.
- [ ] La migración V32 CHECK constraint: en lugar de solo agregar DEVOLUCION al CHECK, ¿deberíamos eliminar el CHECK completamente ahora y migrar a una FK contra `tb_tipos_movimiento`? El spec dice "o eliminar el CHECK o agregar DEVOLUCION". Este diseño opta por agregar DEVOLUCION para minimizar cambios, pero queda abierto si se quiere hacer la migración completa a FK.
- [ ] El modelo `NumeracionControl` usa `synchronized` en el método de servicio para seguridad adicional sobre `FOR UPDATE`. En cluster (Windows Server con 16GB RAM, probablemente single-node), `synchronized` no será bottleneck, pero ¿consideramos escalar a multi-instancia en el futuro?
- [ ] Seed de numeración: ¿qué año usar para las entradas iniciales? Asumimos año actual determinístico, pero ¿debe ser configurable?
