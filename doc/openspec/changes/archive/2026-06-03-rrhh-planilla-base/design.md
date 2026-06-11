# Design: RRHH Planilla — Core Payroll Engine

## Technical Approach

Payroll engine as a new `rrhh.planilla` module layered under `com.clinica.rrhh.planilla` plus a maestro catalog (`ConceptoPlanilla`). Generation runs synchronously in a single `@Transactional` — fetch active contracts → lookup pension info → apply AFP/ONP/EsSalud/Renta 5ta → persist header + lines. Delivered as **2 stacked PRs** on `feature/rrhh-planilla-base` to respect the 400-line review budget.

## Architecture Decisions

### ADR-5: Conceptos as DB catalog, not enums

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Java enum | Fast, compile-safe, no migration cost | **Rejected** — code change per new concept |
| `tb_conceptos_planilla` seed table | Extra migration, lookup cost | **Chosen** — formula ref for future automation |

### ADR-6: Synchronous, idempotent generation

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Async job | Resilient, scales better | **Rejected** — MVP overhead |
| Sync POST with guard | Blocks HTTP thread during calc | **Chosen** — ~40 workers, < 2s. Idempotent: rejects duplicate period |

### ADR-7: JSONB concept per detail row

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Normalized `planilla_conceptos` join table | Queryable, normalized | **Rejected** — over-engineered for MVP |
| `conceptos_json` TEXT on `tb_planilla_detalles` | Audit trail, not queryable by concept | **Chosen** — freeze calculation at generation time |

### ADR-8: RMV/UIT in application.yml

| Option | Tradeoff | Decision |
|--------|----------|----------|
| DB parameter table | Hot-reloadable | **Rejected** — changes annually, one deploy |
| `@ConfigurationProperties` | Needs restart | **Chosen** — simpler, audit trail via git |

### ADR-9: PR split boundary

| Option | Tradeoff | Decision |
|--------|----------|----------|
| Single PR | Exceeds 400-line budget | **Rejected** — reviewer overload |
| PR-A (foundation) + PR-B (engine) | Merges must stack | **Chosen** — PR-A = zero business logic, fast to review |

## Data Model

### tb_conceptos_planilla (maestro, V24)

| Column | Type | Notes |
|--------|------|-------|
| `cpl_id` | BIGSERIAL PK | |
| `cpl_codigo` | VARCHAR(30) UNIQUE | BASICO, ASIGNACION_FAMILIAR, AFP_OBLIGATORIO, ONP_DESCUENTO, ESSALUD_APORTE, RENTA_5TA, ADELANTO |
| `cpl_nombre` | VARCHAR(100) | |
| `cpl_tipo` | VARCHAR(20) | INGRESO, DESCUENTO, APORTE |
| `cpl_formula` | VARCHAR(100) | Optional formula reference |
| `cpl_orden` | INTEGER DEFAULT 0 | |

### tb_periodos_planilla (V25)

| Column | Type | Notes |
|--------|------|-------|
| `ppl_id` | BIGSERIAL PK | |
| `ppl_anio` + `ppl_mes` | INTEGER | UNIQUE(anio, mes) |
| `ppl_fecha_inicio` / `ppl_fecha_fin` | DATE | |
| `ppl_estado` | VARCHAR(20) | ABIERTO, CERRADO, ANULADO |

### tb_planillas (V25)

| Column | Type | Notes |
|--------|------|-------|
| `pla_id` | BIGSERIAL PK | |
| `pla_periodo_planilla_id` | BIGINT FK | → tb_periodos_planilla |
| `pla_fecha_liquidacion` | DATE | |
| `pla_total_ingresos` / `pla_total_descuentos` / `pla_total_aportes` / `pla_total_neto` | NUMERIC(10,2) | |
| `pla_estado` | VARCHAR(20) | BORRADOR, LIQUIDADO |

### tb_planilla_detalles (V25)

One row per worker. `conceptos_json` TEXT stores `{"BASICO": 2500.00, "ASIGNACION_FAMILIAR": 113.00, "AFP_OBLIGATORIO": 68.00}` at generation time.

## Migration Plan

| File | Content |
|------|---------|
| `V24__seed_conceptos_planilla.sql` | CREATE `tb_conceptos_planilla` + INSERT 7 seed rows |
| `V25__rrhh_planilla.sql` | CREATE `tb_periodos_planilla`, `tb_planillas`, `tb_planilla_detalles` + FK indexes |

Rollback: DROP V25 tables (`tb_planilla_detalles` → `tb_planillas` → `tb_periodos_planilla`), DELETE V24 seed, DROP `tb_conceptos_planilla`.

## Entity & Layer Layout

### maestro module (`com.clinica.maestro`)

| Layer | File | Notes |
|-------|------|-------|
| entity | `entity/rrhh/ConceptoPlanilla.java` | Extends BaseEntity, prefix `cpl_` |
| repository | `repository/rrhh/ConceptoPlanillaRepository.java` | `findAllByActivoTrueOrderByOrden()` |
| service | `service/ConceptoPlanillaService.java` | Pass-through `findAll()` |
| controller | `controller/ConceptoPlanillaController.java` | `GET /api/v1/conceptos-planilla`, public |

### rrhh.planilla module (`com.clinica.rrhh.planilla`)

| Layer | File | Notes |
|-------|------|-------|
| entity | `entity/PeriodoPlanilla.java` | `ppl_` prefix |
| entity | `entity/Planilla.java` | `pla_` prefix, FK → PeriodoPlanilla |
| entity | `entity/PlanillaDetalle.java` | `pde_` prefix, FK → Planilla, FK → Trabajador, FK → Contrato |
| repository | `repository/PeriodoPlanillaRepository.java` | `findByAnioAndMes()`, `existsByAnioAndMes()` |
| repository | `repository/PlanillaRepository.java` | CRUD |
| repository | `repository/PlanillaDetalleRepository.java` | `findByPlanillaId()` |
| dto | `dto/PeriodoPlanillaRequest.java` | Record, `@NotNull anio, mes, fechaInicio, fechaFin` |
| dto | `dto/PeriodoPlanillaResponse.java` | Record, custom toString |
| dto | `dto/PlanillaResponse.java` | Record, excludes financial fields from toString |
| dto | `dto/PlanillaDetalleResponse.java` | Record, includes conceptosJson |
| service | `service/PeriodoPlanillaService.java` | create (duplicate guard), cerrar, findAll |
| service | `service/PlanillaLiquidacionService.java` | `generar()` — the engine |
| service | `service/Renta5taCalculator.java` | Helper component |
| controller | `controller/PeriodoPlanillaController.java` | `GET/POST /api/v1/periodos-planilla`, `PUT /{id}/cerrar` |
| controller | `controller/PlanillaController.java` | `GET /api/v1/planillas`, `GET /{id}`, `GET /{id}/detalles`, `POST /generar` |

## Key Flows

```
POST /api/v1/planillas/generar?periodoPlanillaId={id}
  → PeriodoPlanillaService validates ABIERTO + no existing planilla
  → PlanillaLiquidacionService.generar(periodo):
      1. Fetch Trabajadores with ACTIVE Contrato
      2. For each worker:
         a. sueldoBase = contrato.remuneracion
         b. asignacionFamiliar = (cantidadHijos >= 1) ? RMV * 0.10 : 0
         c. Lookup InformacionPensionaria → AFP(tasa + prima) or ONP(13%)
         d. Renta 5ta = Renta5taCalculator.calculate(remuneracion, uit)
         e. EsSalud = remuneracion * 0.09 (aporte, not descuento)
         f. totalIngresos = sueldoBase + asignacionFamiliar
         g. totalDescuentos = afp/onp + renta
         h. totalAportes = essalud
         i. neto = totalIngresos - totalDescuentos
      3. Create Planilla header + PlanillaDetalle rows
      4. Return PlanillaResponse
```

## Configuration (application.yml)

```yaml
rrhh:
  planilla:
    rmv: 1130       # Remuneración Mínima Vital 2026
    uit: 5700       # Unidad Impositiva Tributaria 2026
```

Read via `@ConfigurationProperties(prefix = "rrhh.planilla")` record `RrhhPlanillaProperties`.

## Endpoints

| Method | Path | Authority | PR |
|--------|------|-----------|----|
| `GET` | `/api/v1/conceptos-planilla` | public | A |
| `GET` | `/api/v1/periodos-planilla` | `rrhh:ver` | A |
| `POST` | `/api/v1/periodos-planilla` | `rrhh:editar` | A |
| `PUT` | `/api/v1/periodos-planilla/{id}/cerrar` | `rrhh:editar` | A |
| `GET` | `/api/v1/planillas` | `rrhh:ver` | B |
| `GET` | `/api/v1/planillas/{id}` | `rrhh:ver` | B |
| `GET` | `/api/v1/planillas/{id}/detalles` | `rrhh:ver` | B |
| `POST` | `/api/v1/planillas/generar?periodoPlanillaId=` | `rrhh:editar` | B |

## Testing Strategy

| Layer | What | Approach |
|-------|------|----------|
| Unit | `Renta5taCalculator` — brackets, below-7-UIT, edge cases | `@ExtendWith(MockitoExtension.class)`, known input/output |
| Unit | `PlanillaLiquidacionService` — AFP vs ONP path, empty contracts | Mockito, mock all collaborators |
| Unit | `PeriodoPlanillaService` — duplicate reject, close validation | Mockito |
| Repository | V24/V25 migrations | `@DataJpaTest` with H2, verify tables + seed rows |
| Controller | `PeriodoPlanillaController`, `PlanillaController` | `@WebMvcTest` + `WithMockUser`, assert 200/201/403/409 |

## PR Split Boundary

| PR | Files | Risk |
|----|-------|------|
| **PR-A**: ConceptoPlanilla + PeriodoPlanilla + V24-V25 migrations + `RrhhPlanillaProperties` | ~15 files | Low — structural, no business logic |
| **PR-B**: Planilla + PlanillaDetalle + `PlanillaLiquidacionService` + `Renta5taCalculator` + endpoints + tests | ~15 files | Medium — calculation engine |

Both target `feature/rrhh-planilla-base`. PR-B CI won't pass until PR-A merges (missing entities/migrations).

## Open Questions

- None — all decisions documented. AFP rates from `tb_afp_tasas_historicas` (current active row); seed data confirmed in pension PR.
