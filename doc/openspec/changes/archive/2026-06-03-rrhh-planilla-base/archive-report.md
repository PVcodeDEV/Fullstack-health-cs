# Archive Report

**Change**: rrhh-planilla-base
**Archived**: 2026-06-03
**Mode**: openspec
**Verdict**: PASS

## Overview

Core payroll engine for RRHH module. Built monthly period lifecycle, concept catalog, and full liquidation engine with AFP/ONP/EsSalud/Renta 5ta deductions. Delivered as 2 stacked PRs (PR-A Foundation → PR-B Engine) on `feature/rrhh-planilla-base` to respect the 400-line review budget.

One new capability:
- **rrhh-planilla** — Payroll engine: period management (ABIERTO → CERRADO, no re-open), configurable concept catalog, synchronous liquidation with AFP/ONP deductions, Renta 5ta progressive tax (5 brackets: 8/14/17/20/30%), append-only audit trail

## Specs Synced to Source of Truth

| Domain | Action | Details |
|--------|--------|---------|
| rrhh-planilla | Already in source of truth | PLN-001 through PLN-008 — 8 requirements, 11 scenarios |

No delta specs existed in the change folder — the main spec was written directly during spec phase and already reflects implemented behavior.

**Total**: 8 requirements, 11 scenarios across 1 domain

## Archive Contents

- proposal.md ✅ — Intent, scope (16 items in, 6 out), 3 risks with mitigations, rollback plan, 7 success criteria
- design.md ✅ — 5 ADRs with rationale, data model (4 tables), migration plan, full layer layout, key flows, endpoint table, testing strategy, PR split boundary
- tasks.md ✅ — 17/17 tasks complete across 2 phases (all `[x]`); 2-chained-PR delivery
- archive-report.md ✅ — This file

## Task Completion

| Phase | Tasks | Status |
|-------|-------|--------|
| PR-A: Foundation (Migrations + Conceptos + Periodos) | 7/7 | ✅ Complete |
| PR-B: Engine Entities + Config + DTOs + Services + Tests | 10/10 | ✅ Complete |

**Total**: 17/17 tasks complete — 0 incomplete, 0 deferred

## What Was Built

### Flyway Migrations (2 files)
- `V24__seed_conceptos_planilla.sql` — CREATE `tb_conceptos_planilla` + INSERT 7 seed rows (BASICO, ASIGNACION_FAMILIAR, AFP_OBLIGATORIO, ONP_DESCUENTO, ESSALUD_APORTE, RENTA_5TA, ADELANTO)
- `V25__rrhh_planilla.sql` — CREATE `tb_periodos_planilla`, `tb_planillas`, `tb_planilla_detalles` + FK indexes + constraints

### Entities (4 new)
- **ConceptoPlanilla.java** — `maestro/entity/rrhh/ConceptoPlanilla.java`, extends BaseEntity, `cpl_` prefix, columns: codigo, nombre, tipo (INGRESO/DESCUENTO/APORTE), formula, orden
- **PeriodoPlanilla.java** — `rrhh/planilla/entity/PeriodoPlanilla.java`, extends BaseEntity, `ppl_` prefix, enum estado (ABIERTO/CERRADO/ANULADO), unique(anio, mes)
- **Planilla.java** — `rrhh/planilla/entity/Planilla.java`, `pla_` prefix, FK → PeriodoPlanilla, totals fields (totalIngresos, totalDescuentos, totalAportes, totalNeto), enum estado (BORRADOR/LIQUIDADO)
- **PlanillaDetalle.java** — `rrhh/planilla/entity/PlanillaDetalle.java`, `pde_` prefix, FK → Planilla + Trabajador + Contrato, `conceptos_json` TEXT for per-concept breakdown

### DTOs (4 records)
- **PeriodoPlanillaRequest.java** — `@NotNull` anio, mes, fechaInicio, fechaFin
- **PeriodoPlanillaResponse.java** — id, anio, mes, estado, fechas
- **PlanillaResponse.java** — id, periodo, totals, estado, fechaLiquidacion
- **PlanillaDetalleResponse.java** — trabajador info, sueldoBase, asignacionFamiliar, conceptosJson, totals

### Configuration
- **PlanillaProperties.java** — `@ConfigurationProperties(prefix = "rrhh.planilla")` record with rmv (1130) and uit (5700)
- Added to `application.yml`: `rrhh.planilla.rmv: 1130`, `rrhh.planilla.uit: 5700`

### Services (3)
- **PeriodoPlanillaService.java** — create (duplicate guard → 409 Conflict), cerrar (validates ABIERTO, blocks regenerate on CERRADO), findAll
- **Renta5taCalculator.java** — `@Component`, progressive withholding on 5 brackets: 8% up to 5 UIT, 14% up to 20 UIT, 17% up to 35 UIT, 20% up to 45 UIT, 30% excess. Annual projection = (monthly × 12), deducts 7 UIT, calculates monthly = (annualTax / 12) - alreadyWithheld
- **PlanillaLiquidacionService.java** — Generation engine: fetch active contracts → lookup InformacionPensionaria → apply AFP (tasa + prima) or ONP (13%) → calculate EsSalud (9%), Asignación Familiar (10% RMV), Renta 5ta → persist Planilla header + PlanillaDetalle rows in single `@Transactional`

### Controllers (2)
- **PeriodoPlanillaController.java** — `GET /api/v1/periodos-planilla` (`rrhh:ver`), `POST` (`rrhh:editar`), `PUT /{id}/cerrar` (`rrhh:editar`)
- **PlanillaController.java** — `GET /api/v1/planillas` (`rrhh:ver`), `GET /{id}` (`rrhh:ver`), `GET /{id}/detalles` (`rrhh:ver`), `POST /generar` (`rrhh:editar`)

### Security
- `@PreAuthorize("hasAuthority('rrhh:ver')")` — read endpoints
- `@PreAuthorize("hasAuthority('rrhh:editar')")` — write endpoints
- ConceptoPlanilla list is public (no auth)

### Test Files (46 tests across 11 test classes)
- PR-A: 3 `@DataJpaTest` (ConceptoPlanilla, PeriodoPlanilla, Planilla repos), 2 Mockito (PeriodoPlanillaService), 2 `@WebMvcTest` (ConceptoPlanilla, PeriodoPlanilla controllers)
- PR-B: 2 `@DataJpaTest` (Planilla, PlanillaDetalle), 3 Mockito (Renta5taCalculator — brackets, below-7-UIT, zero; PlanillaLiquidacionService — AFP vs ONP path, empty contracts; PeriodoPlanillaService edge cases), 2 `@WebMvcTest` (PlanillaController — 200/403/409), 1 integration

**Full suite**: 703 tests passing, 0 failed

## Architecture Decisions Implemented

| ADR | Decision | Status |
|-----|----------|--------|
| ADR-5 | Conceptos as DB catalog (seed table, not enums) | ✅ Implemented |
| ADR-6 | Synchronous, idempotent generation (rejects duplicate, blocks HTTP thread) | ✅ Implemented |
| ADR-7 | `conceptos_json` TEXT per detail row (freeze at generation time) | ✅ Implemented |
| ADR-8 | RMV/UIT in `application.yml` via `@ConfigurationProperties` | ✅ Implemented |
| ADR-9 | PR split into PR-A (foundation) + PR-B (engine) for review budget | ✅ Implemented |

## Source of Truth

The following spec already reflects the implemented behavior:
- `doc/openspec/specs/rrhh-planilla/spec.md` — PLN-001 through PLN-008 — 8 requirements, 11 scenarios

## SDD Cycle Complete

The change has been fully planned, implemented, verified, and archived.
Ready for the next change.
