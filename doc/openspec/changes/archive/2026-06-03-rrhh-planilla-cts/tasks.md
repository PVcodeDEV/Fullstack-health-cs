# Tasks: RRHH Planilla — CTS

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~350–450 |
| 400-line budget risk | Medium |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | single-pr |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Medium

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | CTS module (V27 + entity + repo + service + DTO + controller + tests) | PR 1 | Single PR to main; ~350-450 lines, requires size:exception |

## Phase 1: Foundation (V27 + Entity + Repository)

- [x] 1.1 Create `db/migration/V27__rrhh_cts.sql` — `tb_depositos_cts` DDL (15 columns + PK + FKs to tb_periodos_planilla/tb_trabajadores/tb_contratos + UNIQUE(periodo_planilla_id, trabajador_id) + indexes)
- [x] 1.2 Create `rrhh/cts/entity/DepositoCts.java` — extends BaseEntity, `tb_depositos_cts`, `dct_` prefix via `@AttributeOverride`, `@ToString.Exclude` on all monetary fields (remuneracionComputable, promedioGratificacion, promedioBonificacion, montoCts) per CTS-007
- [x] 1.3 Create `rrhh/cts/repository/DepositoCtsRepository.java` — `findByPeriodoPlanillaId()`, `findByPeriodoPlanillaIdAndTrabajadorId()` for upsert lookup

## Phase 2: Service + DTO

- [x] 2.1 Create `rrhh/cts/dto/CtsResponse.java` — record with `fromEntity()` factory, `toString()` excludes all monetary fields per CTS-007
- [x] 2.2 Create `rrhh/cts/service/CtsService.java` — `calcular(periodoPlanillaId)` with: semester derivation (mes=5→NOVIEMBRE, mes=11→MAYO), 30-day truncamiento (day≤14 counts, 15+ skips), RC = sueldo + asigFamiliar + 1/6 avg gratif + 1/6 avg bonif, formula `(RC/360) × días`, zero deductions, upsert idempotency; `findAll()`, `findById()`
- [x] 3.1 Create `rrhh/cts/controller/CtsController.java` — `POST /api/v1/cts/calcular` (`@PreAuthorize rrhh:editar`), `GET /api/v1/cts` + `GET /api/v1/cts/{id}` (`rrhh:ver` class-level)
- [x] 3.2 Write `@DataJpaTest` for DepositoCtsRepository — CRUD, findByPeriodoPlanillaId, findByPeriodoPlanillaIdAndTrabajadorId
- [x] 3.3 Write Mockito tests for CtsService — full semester (180d → RC/2), proportional (120d), mid-month exclusion (day 15+), day 1-14 counts, 0/1/2 gratificaciones, zero months (skip), invalid mes (400), idempotent re-run
- [x] 3.4 Write `@WebMvcTest` for CtsController — POST 201/400/403, GET list 200, GET by id 200/404
- [x] 3.5 Write integration test — create trabajador → create contrato → create periodo → POST calcular → verify `(RC/360)×180` → re-run verifies upsert

**Total: 10 tasks**
