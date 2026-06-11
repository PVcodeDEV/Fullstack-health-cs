# Proposal: Maestro — Catálogos Base

## Intent

Implement all shared master/catalog tables for the ERP Clínico. Maestro is layer zero — every other module depends on these catalogs for foreign key references. Without this change, no domain module can store data.

## Scope

### In Scope
- 19 JPA entities with repositories and CRUD services
- 3 import services (Ubigeo from RENIEC, CIE-10 from MINSA, SUNAT catalogs)
- Flyway V1__ seed migrations per entity (Ubigeo ~2k rows, CIE-10 ~17k rows)
- REST controllers (`/api/v1/maestro/{catalog}`) with DTO + validation
- Unit and integration tests per batch

### Out of Scope
- Thymeleaf CRUD views (separate change)
- Proveedor (stays in farmacia), TipoContrato (stays in rrhh), SerieComprobante (caja)
- External API integration (RENIEC/SUNAT live query)
- CIE-11 migration logic (mechanism only — migration itself deferred)

## Capabilities

> Contract between proposal and specs phases. No existing specs exist — all capabilities are new.

### New Capabilities
- `maestro-documentos-identidad`: TipoDocumentoIdentidad with SUNAT codes
- `maestro-ubigeo`: 3-table normalized geographic division (dpto → prov → dist)
- `maestro-cie10`: CIE-10 diagnostic codes with CIE-11 migration field
- `maestro-catalogos-clinicos`: EspecialidadMedica, TipoPaciente, TipoAtencion, ViaAdministracion, FormaFarmaceutica, TipoHabitacion, TipoDocumentoClinico
- `maestro-catalogos-financieros`: TipoComprobante, TipoMoneda, UnidadMedida (SUNAT-aligned)
- `maestro-catalogos-organizacion`: AreaFuncional, Aseguradora, CategoriaInsumo, EstadoCivil

### Modified Capabilities
None — first domain module.

## Approach

Four batches within a single change:

1. **Batch 1 — Core + Ubigeo**: EstadoCivil, TipoDocumentoIdentidad, Ubigeo (3 tables + import). Seed: ~10 + 2,010 rows.
2. **Batch 2 — Clinical**: CIE-10 (~17k rows via CSV import), EspecialidadMedica, TipoPaciente, TipoAtencion, ViaAdministracion, FormaFarmaceutica, TipoHabitacion, TipoDocumentoClinico.
3. **Batch 3 — SUNAT/Financial**: TipoComprobante, TipoMoneda, UnidadMedida + SUNAT import.
4. **Batch 4 — Organization**: AreaFuncional, Aseguradora, CategoriaInsumo.

Each batch: entity → repository → DTO → service → controller → Flyway migration → tests. Deliver sequentially per batch.

## Affected Areas

| Area | Impact | Description |
|------|--------|-------------|
| `backend/src/main/java/com/clinica/maestro/` | New | Full module (entity/repository/service/dto/controller) |
| `backend/src/main/resources/db/migration/` | New | 19 V1__ seed Flyway migrations |
| `backend/src/test/java/com/clinica/maestro/` | New | Unit + integration tests |

## Risks

| Risk | Likelihood | Mitigation |
|------|------------|------------|
| CIE-10 + Ubigeo seed data strains H2 startup | Medium | Split seed migrations across batches; use Flyway `INIT` |
| H2 vs PostgreSQL dialect in seed SQL | Medium | Test all migrations against PostgreSQL dialect; avoid H2-specific syntax |
| Rollout impact on clinic ops | Low | Catalogs are read-dominated with zero transactions depending on them |

## Rollback Plan

Drop all `maestro.*` tables via manual `DROP TABLE` script. No data loss — catalogs are reference data with no dependent transactions yet.

## Dependencies

None — maestro is layer zero. Module placeholder exists from bootstrap.

## Success Criteria

- [ ] `mvn compile` succeeds with JPA metamodel for all 19 entities
- [ ] `mvn test` passes all repository CRUD tests per batch
- [ ] Flyway migrations execute cleanly on `mvn spring-boot:run` (H2 dev profile)
- [ ] `GET /api/v1/maestro/tipo-documento-identidad` returns seeded data
