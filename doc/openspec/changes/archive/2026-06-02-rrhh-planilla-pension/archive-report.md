# Archive Report

**Change**: rrhh-planilla-pension
**Archived**: 2026-06-02
**Mode**: openspec
**Verdict**: PASS (2 warnings fixed)

## Overview

Pension regime (AFP/ONP) foundation for payroll. Established AFP catalog with historical SBS rate tracking, per-worker pension info with CUSPP validation, and ONP special handling. Implements upsert semantics for single-record-per-worker pension information.

One new capability:
- **rrhh-pension** — AFP catalog (3 entities), historical rate tracking, worker pension info CRUD (GET + PUT upsert), CUSPP validation (12-digit for AFP, auto-populated DNI for ONP), PII protection on cuspp

## Specs Synced to Source of Truth

| Domain | Action | Details |
|--------|--------|---------|
| rrhh-pension | Already in source of truth | PEN-001 through PEN-006 — 6 requirements, 8 scenarios |

**Total**: 6 requirements, 8 scenarios across 1 domain

## Archive Contents

- proposal.md ✅ — Intent, scope (11 items in, 7 out), 3 risks with mitigations, rollback plan, 4 success criteria
- design.md ✅ — 4 ADRs with rationale, data flow diagrams, file changes (13 files), testing strategy
- tasks.md ✅ — 19/19 tasks complete across 3 phases (all `[x]`); single PR delivery
- archive-report.md ✅ — This file

## Task Completion

| Phase | Tasks | Status |
|-------|-------|--------|
| 1. Foundation (V23 + Entities + Repositories) | 6/6 | ✅ Complete |
| 2. Services + DTOs | 5/5 | ✅ Complete |
| 3. Controllers + Tests | 6/6 | ✅ Complete |

**Total**: 19/19 tasks complete — 0 incomplete, 0 deferred

## What Was Built

### Flyway Migration (1 file)
- `V23__rrhh_pension.sql` — CREATE `tb_afps` + seed (5 AFP rows with current SBS 2026 rates), CREATE `tb_afp_tasas_historicas`, CREATE `tb_informacion_pensionaria`, indexes, constraints, FK relationships

### Entities (3 new)
- **Afp.java** — `maestro/entity/rrhh/Afp.java`, extends BaseEntity, columns: `afpId`, `afpCodigo`, `afpNombre`, `afpDescripcion`, `afpActivo`
- **AfpTasaHistorica.java** — `maestro/entity/rrhh/AfpTasaHistorica.java`, extends BaseEntity, columns: `athId`, `athAfpId` (FK), `athTipoComision`, `athTasa`, `athPrimaSeguro`, `athVigenciaDesde`, `athVigenciaHasta`
- **InformacionPensionaria.java** — `rrhh/pension/entity/InformacionPensionaria.java`, columns: `infId`, `infTrabajadorId` (unique FK), `infCuspp`, `infAfpId` (FK), `infComisionTipo`, `infSctr`, `infFechaAfiliacion`, `infEstado`. `@ToString.Exclude` on cuspp for PII

### Repositories (2)
- `maestro/repository/rrhh/AfpRepository.java` — `findAllByActivoTrueOrderByNombre()`
- `rrhh/pension/repository/InformacionPensionariaRepository.java` — `findByTrabajadorId()`, `existsByTrabajadorId()`

### DTOs (3)
- `maestro/dto/rrhh/AfpResponse.java` — record with id, codigo, nombre
- `rrhh/pension/dto/InformacionPensionariaRequest.java` — `@Valid` with `@NotNull afpId`, `@Size cuspp`; ONP validation in service layer
- `rrhh/pension/dto/InformacionPensionariaResponse.java` — record with afpId, comisionTipo, cuspp (`toString` excluded), sctr, fechaAfiliacion, estado

### Services (2)
- `maestro/service/rrhh/AfpService.java` — `findAll()` maps to AfpResponse list
- `rrhh/pension/service/InformacionPensionariaService.java` — `getByTrabajadorId()` returns response or 404; `upsert()` validates CUSPP (12 digits AFP, auto-populate DNI for ONP), null-safe ONP handling, find-or-save semantics

### Controllers (2)
- `maestro/controller/AfpController.java` — `GET /api/v1/afps`, no auth required (public catalog)
- `rrhh/pension/controller/InformacionPensionariaController.java` — `GET /api/v1/trabajadores/{id}/informacion-pensionaria` (`@PreAuthorize rrhh:ver`), `PUT /api/v1/trabajadores/{id}/informacion-pensionaria` (`@PreAuthorize rrhh:editar`)

### Security
- `@PreAuthorize("hasAuthority('rrhh:ver')")` — GET pension info (read)
- `@PreAuthorize("hasAuthority('rrhh:editar')")` — PUT pension info (write)
- AFP catalog is public (no auth required)

### Test Files (7 test classes, 23 pension-specific tests)
- 2 `@DataJpaTest` (AfpRepository, InformacionPensionariaRepository) — find by activo, find by trabajadorId, unique constraint
- 1 Mockito service test (InformacionPensionariaService) — ONP null fields, AFP cuspp 12-digit validation, upsert creates vs updates, EntityNotFoundException
- 2 `@WebMvcTest` (AfpController — GET returns 5 entries; InformacionPensionariaController — GET 200/403/404, PUT upsert creates/updates/rejects invalid cuspp)
- 1 Integration test `RrhhPensionFlowIntegrationTest` — full pension workflow

## Architecture Decisions Implemented

| ADR | Decision | Status |
|-----|----------|--------|
| ADR-1 | One pension info per worker (PUT upsert pattern) | ✅ Implemented |
| ADR-2 | AFP rates as historical table (`tb_afp_tasas_historicas`) | ✅ Implemented |
| ADR-3 | ONP as catalog entry with service-layer rules | ✅ Implemented |
| ADR-4 | CUSPP validation at service layer (12-digit AFP, null ONP) | ✅ Implemented |

## Warnings Fixed During Verify

1. **CUSPP toString exposure** — `@ToString.Exclude` added to `InformacionPensionaria.cuspp` for PII compliance (PEN-006)
2. **ONP null field handling** — Null-safe checks for ONP's nullable `comisionTipo` and `sctr` fields in service layer (PEN-004)

## Build & Test Results

- Build: ✅ `mvn compile` — BUILD SUCCESS
- Tests: ✅ 657 passed, 0 failed, 0 skipped (23 pension-specific + 634 existing)

## Source of Truth

The following spec was already in the source of truth and reflects the implemented behavior:
- `doc/openspec/specs/rrhh-pension/spec.md` — PEN-001 through PEN-006 — 6 requirements, 8 scenarios

## SDD Cycle Complete

The change has been fully planned, implemented, verified, and archived.
Ready for the next change.
