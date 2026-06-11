# Tasks: RRHH Planilla — Gratificación Legal

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~250–350 |
| 400-line budget risk | Low |
| Chained PRs recommended | No |
| Suggested split | Single PR |
| Delivery strategy | single-pr |
| Chain strategy | pending |

Decision needed before apply: No
Chained PRs recommended: No
Chain strategy: pending
400-line budget risk: Low

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Gratificación module (migration + entity + repo + service + DTO + controller + tests) | PR 1 | Single PR to main; well within 400-line budget |

## Phase 1: Foundation (V26 + Entity + Repository)

- [x] 1.1 Create `db/migration/V26__rrhh_gratificacion.sql` — `tb_gratificaciones` DDL + seed GRATIFICACION (INGRESO) and BONIF_EXTRAORDINARIA (APORTE) in ConceptoPlanilla + FK to tb_trabajadores/tb_periodos_planilla/tb_contratos + indexes
- [x] 1.2 Create `rrhh/gratificacion/entity/Gratificacion.java` — extends BaseEntity, `tb_gratificaciones`, `gra_` prefix, `@ToString.Exclude` on monetary fields (gratificacion, bonifExtraordinaria, total) per GRT-007
- [x] 1.3 Create `rrhh/gratificacion/repository/GratificacionRepository.java` — `findByPeriodoPlanillaId()`, `findByPeriodoPlanillaIdAndTrabajadorId()` for upsert lookup

## Phase 2: Service + DTO

- [x] 2.1 Create `rrhh/gratificacion/dto/GratificacionResponse.java` — record with `fromEntity()`, `toString()` excludes monetary fields per GRT-007
- [x] 2.2 Create `rrhh/gratificacion/service/GratificacionService.java` — `calcular(periodoPlanillaId)` with semester derivation (mes=6/12), meses computables (day≤14 rule), Pequeña Empresa ½ sueldo / proportional formula, 9% bonif extraordinaria, zero deductions, upsert logic; `findByPeriodoPlanillaId()`, `findById()`

## Phase 3: Controller + Tests

- [x] 3.1 Create `rrhh/gratificacion/controller/GratificacionController.java` — `POST /api/v1/gratificaciones/calcular` (`@PreAuthorize rrhh:editar`), `GET /api/v1/gratificaciones` (`rrhh:ver`), `GET /api/v1/gratificaciones/{id}` (`rrhh:ver`)
- [x] 3.2 Write `@DataJpaTest` for GratificacionRepository — basic CRUD, findByPeriodoPlanillaId, findByPeriodoPlanillaIdAndTrabajadorId
- [x] 3.3 Write Mockito tests for GratificacionService — full semester (6m → ½ sueldo + 9% bonus), proportional (5m), mid-month exclusion (day 15+), zero months (skip), invalid semester (409), idempotent upsert
- [x] 3.4 Write `@WebMvcTest` for GratificacionController — POST 201/403/409 (IllegalArgumentException maps to 409 in Spring Boot 4), GET list 200, GET by id 200/404
- [x] 3.5 Write integration test — create trabajador → create contrato → create periodo → POST calcular → verify gratificación records match expected amounts → re-run verifies upsert

**Total: 10 tasks**
