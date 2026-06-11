# Tasks: RRHH Planilla — Vacaciones (Pequeña Empresa REMYPE)

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~650–800 |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR 1 (Foundation) → PR 2 (Service) → PR 3 (Controller + Tests) |
| Delivery strategy | ask-on-risk |
| Chain strategy | pending |

Decision needed before apply: Yes
Chained PRs recommended: Yes
Chain strategy: pending
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | Foundation: V28 + entities + repos | PR 1 | Base package; tests: @DataJpaTest |
| 2 | Service: DTOs + VacacionService | PR 2 | Depends on PR 1; tests: Mockito |
| 3 | Controller + all test layers | PR 3 | Depends on PR 2; tests: @WebMvcTest + Integration |

## Phase 1: Foundation (V28 + Entity + Repository)

- [x] 1.1 Create `db/migration/V28__rrhh_vacacion.sql` — DDL for `tb_vacaciones_record` (10 cols + PK + FKs) and `tb_vacaciones_goces` (9 cols + PK + FK) with indexes
- [x] 1.2 Create `rrhh/vacacion/entity/VacacionRecord.java` — extends BaseEntity, `vcr_` prefix, `@ToString.Exclude` on monetary fields per VAC-007
- [x] 1.3 Create `rrhh/vacacion/entity/VacacionGoce.java` — extends BaseEntity, `vgo_` prefix, FK via `@ManyToOne` to VacacionRecord
- [x] 1.4 Create `rrhh/vacacion/repository/VacacionRecordRepository.java` — `findByTrabajadorId()`, `findByTrabajadorIdAndEstado()`
- [x] 1.5 Create `rrhh/vacacion/repository/VacacionGoceRepository.java` — `findByRecordIdOrderByFechaInicio()`

## Phase 2: Service + DTOs

- [x] 2.1 Create `rrhh/vacacion/dto/VacacionRecordResponse.java` — record with `fromEntity()`, excludes monetary fields
- [x] 2.2 Create `rrhh/vacacion/dto/VacacionGoceResponse.java` — record with `fromEntity()`
- [x] 2.3 Create `rrhh/vacacion/dto/ProgramarRequest.java` — `@NotNull trabajadorId, fechaInicio, @Min(7) dias`
- [x] 2.4 Create `rrhh/vacacion/service/VacacionService.java` — `calcular()` (12mo anniversary, idempotent, reduction, auto-expiry), `programar()` (≥7d, ≤pendientes, snap remuneracion), `iniciar()`/`completar()`/`cancelar()` state transitions, finders

## Phase 3: Controller + Tests

- [x] 3.1 Create `rrhh/vacacion/controller/VacacionController.java` — calcular/programar/iniciar/completar/cancelar + GET records/goces; `@PreAuthorize` writes→`rrhh:editar`, reads→`rrhh:ver`
- [x] 3.2 Write `@DataJpaTest` for both repositories — CRUD, find by trabajador/record
- [x] 3.3 Write Mockito tests for VacacionService — 12mo record creation, idempotent re-run, expiry, goce lifecycle (PROGRAMADO→EN_CURSO→COMPLETADO/CANCELADO), min 7d, exceeds balance, invalid transitions, finders
- [x] 3.4 Write `@WebMvcTest` for VacacionController — 201 calcular, 201 programar, 200 transitions, 400 validation/invalid state, 403 auth
- [x] 3.5 Write integration test — crear trabajador→contrato→POST calcular→programar→iniciar→completar→verify full lifecycle

**Total: 13 tasks**
