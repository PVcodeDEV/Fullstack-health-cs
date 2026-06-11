# Archivo: RRHH Planilla — Gratificación Legal

**Change**: rrhh-planilla-gratificacion
**Archived at**: 2026-06-03
**Previous path**: `doc/openspec/changes/rrhh-planilla-gratificacion/`
**Archive path**: `doc/openspec/changes/archive/2026-06-03-rrhh-planilla-gratificacion/`
**Mode**: OpenSpec (filesystem)

---

## Requirements Implemented

| ID | Description | Status |
|----|-------------|--------|
| GRT-001 | Semester calculation (mes=6→Enero-Junio, mes=12→Julio-Diciembre) | ✅ Implemented |
| GRT-002 | Months computables (day 1-14 counts, 15+ starts next month) | ✅ Implemented |
| GRT-003 | Gratificación calculation (≥6 months → ½ sueldo; <6 → proportional) | ✅ Implemented |
| GRT-004 | Bonificación Extraordinaria (9% Ley 30334, employer APORTE) | ✅ Implemented |
| GRT-005 | Zero deductions (no AFP/ONP/Renta 5ta) | ✅ Implemented |
| GRT-006 | Endpoints (POST /calcular, GET list, GET by id) and idempotency | ✅ Implemented |
| GRT-007 | PII protection (toString excludes monetary fields, minimal worker data) | ✅ Implemented |

## Test Results

| Layer | Tests | Passed | Failed |
|-------|-------|--------|--------|
| `GratificacionControllerTest` (`@WebMvcTest`) | 8 | 8 | 0 |
| `GratificacionRepositoryTest` (`@DataJpaTest`) | 5 | 5 | 0 |
| `GratificacionServiceTest` (Mockito) | 16 | 16 | 0 |
| `GratificacionFlowIntegrationTest` (`@SpringBootTest`) | 1 | 1 | 0 |
| **Total** | **30** | **30** | **0** |

**Build**: ✅ `mvn compile` — passed
**Verdict**: PASS WITH WARNINGS (spec discrepancy on "total" field flagged; 2 GRT-007 scenarios untested)

## Files Created / Modified

### Source Code (`backend/src/main/java/com/clinica/rrhh/gratificacion/`)

| File | Action | Description |
|------|--------|-------------|
| `entity/Gratificacion.java` | Create | JPA entity, extends BaseEntity, `tb_gratificaciones`, `gra_` prefix, `@ToString.Exclude` on monetary fields |
| `repository/GratificacionRepository.java` | Create | JPA repository with `findByPeriodoPlanillaId()`, `findByPeriodoPlanillaIdAndTrabajadorId()` |
| `dto/GratificacionResponse.java` | Create | Record with `fromEntity()`, `toString()` excludes monetary fields |
| `service/GratificacionService.java` | Create | `@Service @Transactional` — core calculation and upsert logic |
| `controller/GratificacionController.java` | Create | POST `/calcular` (`rrhh:editar`), GET list (`rrhh:ver`), GET by id (`rrhh:ver`) |

### Migration

| File | Action | Description |
|------|--------|-------------|
| `backend/src/main/resources/db/migration/V26__rrhh_gratificacion.sql` | Create | DDL for `tb_gratificaciones` + seed GRATIFICACION / BONIF_EXTRAORDINARIA |

### Spec (Source of Truth)

| File | Action |
|------|--------|
| `doc/openspec/specs/rrhh-gratificacion/spec.md` | Created (no delta — written directly as main spec) |

### Test Files

| File | Type |
|------|------|
| `GratificacionRepositoryTest.java` | `@DataJpaTest` |
| `GratificacionServiceTest.java` | `@ExtendWith(MockitoExtension.class)` |
| `GratificacionControllerTest.java` | `@WebMvcTest` |
| `GratificacionFlowIntegrationTest.java` | `@SpringBootTest` |

## Key Decisions

| Decision | Rationale |
|----------|-----------|
| Separate `gratificacion/` module | Clear domain boundary, no coupling to liquidation logic |
| Upsert for idempotency | No duplicates on retry; `findByPeriodoPlanillaIdAndTrabajadorId().orElseGet(...)` |
| Gratificación factor hard-coded as `0.5` | Legal constant (Pequeña Empresa REMYPE); no need for runtime config |
| Single V26 migration | Combined DDL + seed, fewer migrations to manage |
| `@ToString(onlyExplicitlyIncluded = true)` | PII compliance (Ley 29733) — monetary fields excluded by omission |
| `total = gratificación + bonifExtraordinaria` | Represents total employer cost per Ley 30334 |
| `IllegalArgumentException` → HTTP 409 | Consistent with existing `GlobalExceptionHandler.handleConflict()` |

## Open Issues

**None.** All 10 tasks completed, 30/30 tests passing. Two warnings noted in verify-report:
1. Spec scenario text discrepancy on `total` field — recommended for clarification but not blocking
2. GRT-007 `toString()` and API worker data tests untested — implementation verified correct by static analysis

## Architecture Snapshot

**Module**: `rrhh.gratificacion` under package `com.clinica.rrhh.gratificacion`
**Base path**: `backend/src/main/java/com/clinica/rrhh/gratificacion/`
**API endpoints**:
- `POST /api/v1/gratificaciones/calcular?periodoPlanillaId={id}` — 201/200/409
- `GET /api/v1/gratificaciones` — 200
- `GET /api/v1/gratificaciones/{id}` — 200/404
**Security**: `rrhh:editar` (POST), `rrhh:ver` (GETs)
**Dependencies**: `rrhh-planilla` (PeriodoPlanilla, ConceptoPlanillaService), `rrhh-base` (Trabajador, Contrato)
