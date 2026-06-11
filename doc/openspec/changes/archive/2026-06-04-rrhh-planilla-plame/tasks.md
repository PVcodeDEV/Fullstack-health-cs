# Tasks: RRHH Planilla — PLAME y T-Registro (SUNAT)

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~900–1200 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation) → PR 2 (T-Registro) → PR 3 (PLAME) → PR 4 (Tests) |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Work Units por PR (stacked-to-main)

| Unit | Goal | PR | Scope (tasks) | Notes |
|------|------|----|---------------|-------|
| 1 | Foundation | ✅ PR 1 | 1.1-1.5, 4.3 | ✅ Applied |
| 2 | T-Registro (service + controller) | PR 2 | 2.1, 2.2, 4.1, 4.4, 4.6 T-Registro | Apunta a main |
| 3 | PLAME (service + controller) | PR 3 | 3.1, 3.2, 4.2, 4.5, 4.6 PLAME | Apunta a main |
| 4 | Integration tests | PR 4 | 4.7 | Apunta a main |

## Phase 1: Foundation (V29 + Entity + Repository)

- [x] 1.1 Create `db/migration/V29__rrhh_plame.sql` — DDL for `tb_tregistro_eventos` and `tb_archivos_planilla` with PKs, FKs, indexes
- [x] 1.2 Create `rrhh/planillaplame/entity/TRegistroEvento.java` — `@Table("tb_tregistro_eventos")`, extends BaseEntity, `@ToString(onlyExplicitlyIncluded)` per PLM-006
- [x] 1.3 Create `rrhh/planillaplame/entity/ArchivoPlanilla.java` — `@Table("tb_archivos_planilla")`, extends BaseEntity, SHA-256 hash field, `@ToString(onlyExplicitlyIncluded)` on non-PII
- [x] 1.4 Create `rrhh/planillaplame/repository/TRegistroEventoRepository.java` — `findByPeriodoPlanillaIdOrderByFechaEventoAsc()`, `findByTrabajadorIdOrderByFechaEventoDesc()`, `findByTipoEvento()`, `existsByPeriodoPlanillaId()`
- [x] 1.5 Create `rrhh/planillaplame/repository/ArchivoPlanillaRepository.java` — `findByPeriodoPlanillaId()`, `findByPeriodoPlanillaIdAndTipo()`, `existsByPeriodoPlanillaIdAndTipo()`

## Phase 2: T-Registro (DTOs + Service)

- [x] 2.1 Create `rrhh/planillaplame/dto/TRegistroEventoResponse.java` — record with `fromEntity()`, excludes PII fields
- [x] 2.2 Create `rrhh/planillaplame/service/TRegistroService.java` — scan contratos (ALTA/BAJA/SUSPENSION/REINICIO), scan pension changes (VARIACION), generate TXT, upsert ArchivoPlanilla(arp_tipo='T_REGISTRO'), validate CERRADO

## Phase 3: PLAME (DTOs + Service)

- [x] 3.1 Verify `rrhh/planillaplame/dto/ArchivoPlanillaResponse.java` exists and is correct — record with `fromEntity()`, metadata only (id, tipo, periodo, hash, timestamps)
- [x] 3.2 Create `rrhh/planillaplame/service/PlameService.java` — validate CERRADO, aggregate income (base+gratif+CTS+vacaciones), deductions (AFP/ONP+Renta 5ta+EsSalud 9%), generate 5 SUNAT pipe-delimited files:
  - `.rem` (Estructura 18): una línea por concepto por trabajador
  - `.jor` (Estructura 14): horas ordinarias y sobretiempo
  - `.snl` (Estructura 15): suspensiones/no laborados
  - `.or5` (Estructura 12): otras rentas 5ta categoría
  - `.toc` (Estructura 26): condiciones (AFP/ONP, seguro, domiciliado)
  - upsert tb_archivos_planilla por tipo (REM|JOR|SNL|OR5|TOC)

## Phase 4: Controllers + Tests

- [x] 4.1 Create `rrhh/planillaplame/controller/TRegistroController.java` — POST `/t-registro/generar`, GET `/t-registro/eventos`, GET `/t-registro/archivos/{id}/descargar`; `@PreAuthorize`
- [x] 4.2 Create `rrhh/planillaplame/controller/PlameController.java` — POST `/plame/generar` (returns 5 ArchivoPlanilla), GET `/plame/archivos`, GET `/plame/archivos/{id}/descargar`, GET `/plame/descargar?periodoPlanillaId=&tipo=`, GET `/plame/descargar-zip?periodoPlanillaId=`; `@PreAuthorize`
- [x] 4.3 Write `@DataJpaTest` for both repos — CRUD, findByPeriodo+Tipo, find by trabajador, exists checks
- [x] 4.4 Write Mockito tests for TRegistroService — ALTA/BAJA/VARIACION detection, empty period, TXT format
- [x] 4.5 Write Mockito tests for PlameService — aggregation math, 409 for ABIERTO, empty data, AFP vs ONP regimes
- [x] 4.6 Write `@WebMvcTest` for TRegistroController — 201/200 generar, 409 ABIERTO, 403 auth, file download Content-Type
- [x] 4.6b Write `@WebMvcTest` for PlameController — generar (returns 5 files), descarga por tipo, ZIP download
- [x] 4.7 Write `@SpringBootTest` integration — full T-Registro + PLAME generation for CERRADO period

**Total: 15 tasks**
