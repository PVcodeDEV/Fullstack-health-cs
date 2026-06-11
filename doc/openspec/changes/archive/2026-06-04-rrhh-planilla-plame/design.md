# Design: RRHH Planilla — PLAME y T-Registro (SUNAT)

## Technical Approach

New `planillaplame` module under `com.clinica.rrhh` with two services: **TRegistroService** scans contrato/pension history per CERRADO period to produce labor events; **PlameService** aggregates per-worker income (planilla, gratif, CTS, vacaciones) and deductions (AFP/ONP, Renta 5ta, EsSalud) into 5 SUNAT import files (.rem, .jor, .snl, .or5, .toc). Files stored in `tb_archivos_planilla` with SHA-256 integrity hash. No scheduled triggers — manual generation only.

## Architecture Decisions

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Spring events vs. explicit scan for T-Registro | Events: real-time hooks but modify 3 existing services. Scan: no side effects, correct for "what happened in period X" | **Explicit scan** — simpler, no coupling to Contrato/Pension service |
| Structured TXT vs. SUNAT file formats (.rem, .jor, .snl) | Structured: legible pero no sirve para PDT PLAME. SUNAT formats: el PDT PLAME los importa directamente. | **SUNAT file formats** — generar `.rem`, `.jor`, `.snl`, `.or5`, `.toc` por periodo, listos para cargar en el PDT PLAME v4.6 |
| On-the-fly generation vs. DB storage | On-the-fly: no storage needs. DB: audit trail, idempotent update, traceability | **DB storage** — `contenido` as TEXT, re-generate updates same record |
| Single row vs. multiple file rows per period | Single row with all content: simpler pero no permite descarga individual de cada archivo. Multiple rows: cada archivo SUNAT es un registro independiente. | **Multiple rows** — un registro por archivo (REM, JOR, SNL, OR5, TOC, T_REGISTRO), cada uno descargable individualmente |
| ZIP download vs. individual downloads | Ambos. ZIP útil para cargar todo al PDT PLAME de una. | **Ambos** — descarga individual + ZIP con todos los archivos del periodo |
| Package: `planillaplame` vs. extending `planilla` | Extending: coupling. Separate module: clean boundary | **`com.clinica.rrhh.planillaplame`** — new 5-layer module, no planilla module coupling |

## Data Flow

```
POST /api/v1/plame/generar?periodoPlanillaId=X
  │
  ├─→ Validar PeriodoPlanilla CERRADO
  ├─→ Obtener PlanillaDetalle + Gratif + CTS + Vacaciones + Pension
  ├─→ Por cada trabajador:
  │     ├─→ .rem (Estructura 18): líneas por concepto (BASICO, ASIG_FAM, AFP, ONP, ESSALUD, RENTA_5TA, etc.)
  │     ├─→ .jor (Estructura 14): horas ordinarias y sobretiempo
  │     ├─→ .snl (Estructura 15): días subsidiados / no laborados
  │     ├─→ .or5 (Estructura 12): otras rentas 5ta categoría
  │     └─→ .toc (Estructura 26): condiciones del trabajador (AFP/ONP, seguro, domiciliado)
  ├─→ Generar archivos pipe-delimited
  ├─→ Upsert tb_archivos_planilla por tipo (REM, JOR, SNL, OR5, TOC)
  └─→ Devolver metadatos de los 5 archivos

POST /api/v1/t-registro/generar?periodoPlanillaId=X
  │
  ├─→ Scan contratos: created/terminated/suspended in periodo
  ├─→ Scan pension changes in periodo
  ├─→ Create TRegistroEvento records
  ├─→ Format T-Registro TXT
  └─→ Upsert tb_archivos_planilla (tipo=T_REGISTRO)
```

## Entity Model

```
tb_tregistro_eventos                tb_archivos_planilla (V29)
├── tre_id BIGSERIAL PK             ├── arp_id BIGSERIAL PK
├── tre_trabajador_id BIGINT FK     ├── arp_periodo_planilla_id BIGINT FK
├── tre_contrato_id BIGINT          ├── arp_tipo VARCHAR(20)
├── tre_tipo_evento VARCHAR(20)     │   REM | JOR | SNL | OR5 | TOC
│   (ALTA|BAJA|VARIACION|          │   | T_REGISTRO
│    SUSPENSION|REINICIO)           ├── arp_contenido TEXT
├── tre_fecha_evento DATE           ├── arp_hash VARCHAR(64)  -- SHA-256
├── tre_periodo_planilla_id FK      ├── arp_generado_por VARCHAR(100)
├── tre_detalle_json TEXT           ├── arp_activo BOOLEAN
├── tre_estado VARCHAR(20)          └── ... BaseEntity columns
└── ... BaseEntity columns
```

Indexes: `(arp_periodo_planilla_id, arp_tipo)` — multiple rows per period, one per file type.

### SUNAT File Structure (PDT PLAME v4.6)

Cada archivo es pipe-delimited (`|`), UTF-8, sin BOM, sin header ni footer. Una línea por registro.

| Archivo | Tipo | Estructura SUNAT | Líneas |
|---------|------|-------------------|--------|
| `.rem`  | Estructura 18 | `TipoDoc|NroDoc|CodigoConcepto|MontoDevengado|MontoPagado` | Una por concepto por trabajador |
| `.jor`  | Estructura 14 | `TipoDoc|NroDoc|HorasOrdinarias|MinutosOrdinarios|HorasSobretiempo|MinutosSobretiempo` | Una por trabajador |
| `.snl`  | Estructura 15 | `TipoDoc|NroDoc|TipoSuspension|DiasSuspension` | Una por suspensión por trabajador |
| `.or5`  | Estructura 12 | `TipoDoc|NroDoc|RUCotroEmpleador|MontoRenta5ta` | Una por trabajador con renta de 5ta |
| `.toc`  | Estructura 26 | `TipoDoc|NroDoc|IndicadorAportePension|IndicadorSeguroVida|IndicadorFDSA|Domiciliado` | Una por trabajador |

**TipoDoc / NroDoc**: RUC `(6)` para el empleador, DNI `(1)` / CE `(4)` / Pasaporte `(7)` para trabajadores.

**CodigoConcepto** (`.rem`): Usa los códigos de la **Tabla Paramétrica 22** (Ingresos, Tributos y Descuentos) de SUNAT:

| Código | Descripción | Tipo |
|--------|-------------|------|
| `0121` | Remuneración o jornal básico | Ingreso |
| `0201` | Asignación Familiar | Asignación |
| `0401` | Gratificaciones de Fiestas Patrias y Navidad | Gratificación |
| `0904` | Compensación por Tiempo de Servicios - CTS | Ingreso-varios |
| `0118` | Remuneración vacacional | Ingreso |
| `0607` | SNP - D.L.19990 (ONP) | Tributo-trabajador |
| `0608` | SPP - Aportación Obligatoria (AFP) | Tributo-trabajador |
| `0605` | Renta Quinta Categoría Retenciones | Tributo-trabajador |
| `0804` | EsSalud (Seguro Regular) | Tributo-empleador |

Para cada trabajador, se emite una línea por cada código que aplique. Los códigos de tributo (`06xx`, `08xx`) usan `MontoDevengado = MontoPagado` (el valor del descuento o aporte).

**TipoSuspension** (`.snl`): `01` (Subsidiado), `02` (No laborado pagado), `03` (Suspensión perfecta), `04` (Huelga).

## File Changes

| File | Action | Status |
|------|--------|--------|
| `db/migration/V29__rrhh_plame.sql` | Create | ✅ PR #1 |
| `rrhh/planillaplame/entity/TRegistroEvento.java` | Create | ✅ PR #1 |
| `rrhh/planillaplame/entity/ArchivoPlanilla.java` | Create | ✅ PR #1 |
| `rrhh/planillaplame/repository/TRegistroEventoRepository.java` | Create | ✅ PR #1 |
| `rrhh/planillaplame/repository/ArchivoPlanillaRepository.java` | Create | ✅ PR #1 |
| `rrhh/planillaplame/dto/TRegistroEventoResponse.java` | Create | Pending |
| `rrhh/planillaplame/dto/ArchivoPlanillaResponse.java` | Create | Pending |
| `rrhh/planillaplame/service/TRegistroService.java` | Create | Pending |
| `rrhh/planillaplame/service/PlameService.java` | Create | Pending |
| `rrhh/planillaplame/controller/PlameController.java` | Create | Pending |
| `rrhh/planillaplame/controller/TRegistroController.java` | Create | Pending |

## Service Algorithms

**TRegistroService.generar(periodoPlanillaId)**:
1. Load PeriodoPlanilla (validate CERRADO)
2. Find all contratos where `fechaInicio` falls within periodo → ALTA events
3. Find contratos where `fechaFin` or `motivoCese` set within periodo → BAJA events
4. Find contratos with `SUSPENDIDO` set in periodo → SUSPENSION; `ACTIVO` after SUSPENDIDO → REINICIO
5. Find pension info changes via `updatedAt` in periodo → VARIACION
6. Upsert events (idempotent per trabajador+tipo+periodo)
7. Format TXT: header + event lines + footer
 8. Save/update ArchivoPlanilla(arp_tipo='T_REGISTRO')

**PlameService.generar(periodoPlanillaId)**:
1. Load PeriodoPlanilla (validate CERRADO)
2. Get PlanillaDetalle for this period (workers + base + asignación familiar)
3. Per worker: fetch gratif, CTS, vacaciones pagadas from respective services
4. Per worker: determine pension regime (AFP/ONP) from InformacionPensionariaService
 5. **Generate .rem** (Estructura 18): for each worker, emit one pipe-delimited line per concept using SUNAT Tabla 22 codes:
   - `0121` (Remuneración o jornal básico) — sueldo base mensual
   - `0201` (Asignación Familiar) — si aplica
   - `0401` (Gratificaciones) — monto del período
   - `0904` (CTS) — monto del período
   - `0118` (Remuneración vacacional) — monto del período
   - `0608` (AFP - Aportación Obligatoria SPP) — descuento del trabajador (si AFP)
   - `0607` (SNP - D.L.19990) — descuento del trabajador (si ONP)
   - `0804` (EsSalud Seguro Regular) — 9% REMYPE (aporte empleador)
   - `0605` (Renta Quinta Categoría Retenciones) — retención, si aplica
6. **Generate .jor** (Estructura 14): for each worker, horas ordinarias (240 for full-time mensual) y sobretiempo (0 default; se puede registrar manualmente)
7. **Generate .snl** (Estructura 15): for each worker with suspensiones, tipo + días. Para REMYPE sin suspensiones: archivo vacío o sin línea.
8. **Generate .or5** (Estructura 12): for each worker with Renta 5ta de otro empleador; vacío si no aplica.
9. **Generate .toc** (Estructura 26): for each worker, indicadores:
   - IndicadorAportePension: `1` (AFP) o `2` (ONP) o `0` (ninguno)
   - IndicadorSeguroVida: `1` (Seguro Vida Ley) o `0` (no)
   - IndicadorFDSA: `1` (Fondo de Desempleo) o `0`
   - Domiciliado: `1` (domiciliado) o `0` (no domiciliado)
10. Upsert each file: `tb_archivos_planilla(arp_tipo=REM|JOR|SNL|OR5|TOC)` — one row per tipo, idempotent per periodo+tipo
11. Return list of ArchivoPlanilla metadata (5 rows)

## Interfaces / Contracts

```java
// PLAME — genera 5 archivos SUNAT por periodo
POST   /api/v1/plame/generar?periodoPlanillaId={id}
       → 201 + ArchivoPlanillaResponse[]  // first generation, 5 files
       → 200 + ArchivoPlanillaResponse[]  // re-generation (update)

GET    /api/v1/plame/archivos?periodoPlanillaId={id}
       → 200 + List<ArchivoPlanillaResponse>

GET    /api/v1/plame/archivos/{id}/descargar
       → 200 + text/plain; charset=UTF-8  // individual file
       Content-Disposition: attachment; filename="{ruc}.{tipo}"

GET    /api/v1/plame/descargar?periodoPlanillaId={id}&tipo=REM
       → 200 + text/plain; charset=UTF-8  // specific type via query
       Content-Disposition: attachment; filename="{ruc}.rem"

GET    /api/v1/plame/descargar-zip?periodoPlanillaId={id}
       → 200 + application/zip          // all 5 files zipped
       Content-Disposition: attachment; filename="{ruc}-{periodo}.zip"

// T-Registro
POST   /api/v1/t-registro/generar?periodoPlanillaId={id}
       → 201 + ArchivoPlanillaResponse   // single file
GET    /api/v1/t-registro/eventos?periodoPlanillaId={id}
       → 200 + List<TRegistroEventoResponse>
GET    /api/v1/t-registro/archivos/{id}/descargar
       → 200 + text/plain; charset=UTF-8
       Content-Disposition: attachment; filename="{ruc}.treg"

// Security
@PreAuthorize("hasAuthority('rrhh:ver')")   class-level GET
@PreAuthorize("hasAuthority('rrhh:editar')") on POST generar
```

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Repository | CRUD, findByPeriodoTipo, upsert behavior | `@DataJpaTest`, H2 |
| Service | T-Registro: event detection from contrato/pension data. PLAME: aggregation math, edge cases (no data, missing benefits, AFP vs ONP) | `@ExtendWith(MockitoExtension.class)` |
| Controller | 201/200 generar, 409 ABIERTO, 403 auth, 404, file download Content-Type | `@WebMvcTest` + `@WithMockUser` |
| Integration | Full flow: T-Registro + PLAME generation for CERRADO period | `@SpringBootTest` + `@AutoConfigureMockMvc` |

## Migration / Rollout

V29: creates `tb_tregistro_eventos` and `tb_archivos_planilla`. Rollback: `DROP TABLE IF EXISTS tb_tregistro_eventos, tb_archivos_planilla;`

## Suggested PR Split

| PR | Scope | Files | Lines (est.) |
|----|-------|-------|-------------|
| 1 | Foundation — V29 + entities + repos + DTOs | V29, 2 entities, 2 repos, 2 DTOs | ✅ ~250 (applied) |
| 2 | T-Registro service + controller | TRegistroService, TRegistroController | ~300 |
| 3 | PLAME service + controller — generar 5 SUNAT files (.rem, .jor, .snl, .or5, .toc) + ZIP download | PlameService, PlameController | ~450 |
| 4 | Tests across all layers | Repository + Service + Controller + Integration | ~400 |

## Open Questions

- [ ] AFP commission type (FLUJO vs. MIXTA) — rates differ for EsSalud. Default to FLUJO for v1?
- [ ] T-Registro VARIACION detection: pension changes only, or also salary changes? For v1, focus on pension regime changes (AFP → ONP, ONP → AFP).
