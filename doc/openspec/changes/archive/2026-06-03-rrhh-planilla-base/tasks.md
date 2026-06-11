# Tasks: RRHH Planilla — Core Payroll Engine

## Review Workload Forecast

| Field | Value |
|-------|-------|
| Estimated changed lines | ~600–700 (PR-A: ~250–350, PR-B: ~400–500) |
| 400-line budget risk | High |
| Chained PRs recommended | Yes |
| Suggested split | PR-A (Foundation) → PR-B (Engine) |
| Delivery strategy | auto-chain |
| Chain strategy | feature-branch-chain |

Decision needed before apply: No
Chained PRs recommended: Yes
Chain strategy: feature-branch-chain
400-line budget risk: High

### Suggested Work Units

| Unit | Goal | Likely PR | Notes |
|------|------|-----------|-------|
| 1 | ConceptoPlanilla + PeriodoPlanilla + V24/V25 migrations | PR-A | Targets `feature/rrhh-planilla-base`; ~250–350 lines, structural only |
| 2 | Planilla engine + Detalle + DTOs + services + controllers + tests | PR-B | Targets `feature/rrhh-planilla-base`; ~400–500 lines, calculation engine |

## Phase 1: PR-A — Foundation (Migrations + Conceptos + Periodos)

- [x] A1 Create `V24__rrhh_conceptos_planilla.sql` — CREATE `tb_conceptos_planilla` + INSERT 7 seed rows (BASICO, ASIGNACION_FAMILIAR, AFP_OBLIGATORIO, ONP_DESCUENTO, ESSALUD_APORTE, RENTA_5TA, ADELANTO)
- [x] A2 Create `entity/rrhh/ConceptoPlanilla.java` at `com.clinica.maestro.entity.rrhh` — extends BaseEntity, `cpl_` prefix
- [x] A3 Create `repository/rrhh/ConceptoPlanillaRepository.java` — `findAllByActivoTrueOrderByOrden()`
- [x] A4 Create `controller/ConceptoPlanillaController.java` — `GET /api/v1/conceptos-planilla`, unauthenticated
- [x] A5 Create `V25__rrhh_planilla_tables.sql` — CREATE `tb_periodos_planilla`, `tb_planillas`, `tb_planilla_detalles` + FK indexes + constraints
- [x] A6 Create `entity/PeriodoPlanilla.java` at `com.clinica.rrhh.planilla.entity` — extends BaseEntity, `ppl_` prefix, enum estado
- [x] A7 Create `repository/PeriodoPlanillaRepository.java` — `findByAnioAndMes()`, `existsByAnioAndMes()`, `findAllByOrderByAnioDescMesDesc()`

## Phase 2: PR-B — Engine Entities + Config + DTOs

- [x] B1 Create `entity/Planilla.java` at `com.clinica.rrhh.planilla.entity` — `pla_` prefix, FK → PeriodoPlanilla, total fields
- [x] B2 Create `entity/PlanillaDetalle.java` at `com.clinica.rrhh.planilla.entity` — `pde_` prefix, FK → Planilla + Trabajador + Contrato, `conceptos_json` TEXT
- [x] B3 Create `PlanillaRepository` + `PlanillaDetalleRepository` — `findByPeriodoPlanillaId()`, `findByPlanillaId()`
- [x] B4 Create DTOs: `PeriodoPlanillaRequest/Response`, `PlanillaResponse`, `PlanillaDetalleResponse` — records with `@Valid` constraints
- [x] B5 Create `PlanillaProperties` record (`@ConfigurationProperties(prefix = "rrhh.planilla")`) + add `rmv: 1130` / `uit: 5700` to `application.yml`
- [x] B6 Create `PeriodoPlanillaService` — create (duplicate guard, 409), cerrar (validates ABIERTO, blocks regenerate), findAll
- [x] B7 Create `Renta5taCalculator` — `@Component`, progressive withholding: deduc 7 UIT → brackets 8%/14%/17%/20%/30% → monthly = (annualTax / 12) - alreadyWithheld
- [x] B8 Create `PlanillaLiquidacionService` — generation engine: fetch active contracts → lookup pension → apply AFP(tasa+prima)/ONP(13%)/EsSalud(9%)/Renta5ta → persist header + detalle in single `@Transactional`
- [x] B9 Create `PeriodoPlanillaController` + `PlanillaController` — all endpoints with `@PreAuthorize("hasAuthority('rrhh:ver')")` on reads, `hasAuthority('rrhh:editar')` on writes
- [x] B10 Write tests: `@DataJpaTest` for PlanillaRepository; Mockito for `PeriodoPlanillaService` (duplicate reject, close validation), `Renta5taCalculator` (brackets, below-7-UIT), `PlanillaLiquidacionService` (AFP vs ONP path, empty contracts); `@WebMvcTest` for both controllers (200/201/403/409); integration test for full generate flow
