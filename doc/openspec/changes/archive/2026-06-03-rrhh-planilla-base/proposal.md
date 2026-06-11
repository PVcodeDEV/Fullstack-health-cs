# Proposal: RRHH Planilla — Core Payroll Engine

## Intent

Without a payroll engine, the clinic cannot liquidate monthly salaries — the single most critical HR operation. This PR builds the core: monthly periods, configurable concepts, salary liquidation with AFP/ONP/EsSalud/Renta 5ta deductions, and payroll line items.

## Scope

### In Scope
- ConceptoPlanilla catalog in maestro (seed: BASICO, ASIGNACION_FAMILIAR, AFP_OBLIGATORIO, ONP_DESCUENTO, ESSALUD_APORTE, RENTA_5TA, ADELANTO)
- PeriodoPlanilla CRUD + close (ABIERTO → CERRADO, no ANULAR)
- Planilla generation engine: calculate ingresos, descuentos, aportes, neto per worker
- PlanillaDetalle per-worker line items with full breakdown
- Flyway V24 (seed conceptos) + V25 (planilla tables)
- Endpoints: periodos-planilla CRUD, planillas list/detail, generar, conceptos list
- Tests: @DataJpaTest + Mockito services + @WebMvcTest controllers

### Out of Scope
- Gratificaciones (PR#3), CTS (PR#4), Vacaciones (PR#5), PLAME (PR#6)
- Approval workflows, bank file generation, email delivery
- Concepto formula engine (formulas are hardcoded in service for v1)
- Editar/eliminar planilla after generation (append-only semantics)

## Capabilities

### New Capabilities
- `rrhh-planilla`: Payroll engine — period management, configurable concepts, monthly liquidation with AFP/ONP/EsSalud/Renta 5ta, append-only audit trail

### Modified Capabilities
None

## Approach

Liquidation engine as a dedicated `PlanillaLiquidacionService` with injectable `CalculadorDescuento` strategies per concepto. Periods are append-only: ABIERTO → CERRADO (no re-open). Planilla generation: fetch active contracts → lookup pension info → calculate each line → persist header + lines in one `@Transactional`. RMV/UIT values in `application.yml` with `@ConfigurationProperties`.

**Delivery**: This PR WILL exceed 400 lines. **Split into 2 stacked PRs**: PR-A (Conceptos + PeriodoPlanilla + migrations) and PR-B (Planilla engine + Detalle + endpoints). Both target a `feature/rrhh-planilla-base` tracker branch.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `maestro/entity/rrhh/ConceptoPlanilla.java` | New | Catalog entity |
| `maestro/{repository,service,controller,dto}/ConceptoPlanilla*` | New | Full layer |
| `rrhh/planilla/{entity,repository,service,dto,controller}/` | New | 3 entities + full layers |
| `rrhh/type/EstadoPeriodoPlanilla.java` | New | Enum: ABIERTO, CERRADO, ANULADO |
| `db/migration/V24__seed_conceptos_planilla.sql` | New | Seed 7 conceptos |
| `db/migration/V25__rrhh_planilla.sql` | New | DDL: periodos, planillas, detalle |
| `application.yml` | Modified | Add rmv.2026, uit.2026 config |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| Renta 5ta progressive calc wrong | Med | Unit-test with known bracket cases; use TDD for tax table |
| AFP rate out of sync at liquidation | Low | Read live from tb_afp_tasas_historicas at generation time |
| Performance with 40+ employees | Low | Single transaction, batch-sized |

## Rollback Plan

Drop V25 (`DROP TABLE tb_planilla_detalles, tb_planillas, tb_periodos_planilla`) + V24 seed revert. No data loss — source tables (contratos, pension) unchanged. PR-B revert keeps PR-A intact.

## Dependencies

- `rrhh-pension` (PR#1, archived) — InformacionPensionaria for AFP/ONP rates
- `rrhh-base` (archived) — Trabajador.cantidadHijos, Contrato.remuneracion
- `maestro-rrhh` — AfpTasaHistorica for current pension rates

## Success Criteria

- [ ] `POST /api/v1/periodos-planilla` creates ABIERTO period, rejects duplicate
- [ ] `POST /api/v1/planillas/generar` creates header + 1 line per active contract
- [ ] AFP descuento matches tasa + prima from tb_afp_tasas_historicas
- [ ] Asignación familiar = 10% RMV when cantidadHijos ≥ 1
- [ ] EsSalud = 9% remuneracion computable (aporte, no descuento)
- [ ] Renta 5ta withholding uses progressive brackets
- [ ] V24 + V25 apply and rollback cleanly on H2 + PostgreSQL
